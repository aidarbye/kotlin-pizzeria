package com.example.data

import com.example.data.local.CartDao
import com.example.data.local.CartLineEntity
import com.example.data.local.MenuDao
import com.example.data.local.MenuItemEntity
import com.example.data.local.toCartEntity
import com.example.data.local.toModel
import com.example.model.Cart
import com.example.model.Extra
import com.example.model.OrderLine
import com.example.model.PromoCode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine

/** Cart lines are stored in Room, so the cart survives a restart. The applied promo code lives in memory. */
class CartRepository(
    private val cartDao: CartDao,
    private val menuDao: MenuDao,
    private val extras: List<Extra>,
    private val externalScope: CoroutineScope
) {
    private val promo = MutableStateFlow<PromoCode?>(null)

    val cart: Flow<Cart> = combine(cartDao.observeLines(), menuDao.observeAll(), promo, ::buildCart)

    suspend fun currentCart(): Cart = buildCart(cartDao.getLines(), menuDao.getAll(), promo.value)

    suspend fun add(line: OrderLine) = externalScope.runDetached { cartDao.addOrIncrease(line.toCartEntity()) }

    suspend fun addAll(lines: List<OrderLine>) = externalScope.runDetached {
        lines.forEach { cartDao.addOrIncrease(it.toCartEntity()) }
    }

    suspend fun changeQuantity(key: String, delta: Int) = externalScope.runDetached { cartDao.changeQuantity(key, delta) }

    suspend fun removeOneOf(itemId: Int) = externalScope.runDetached {
        cartDao.getLastLineOf(itemId)?.let { cartDao.changeQuantity(it.key, -1) }
    }

    suspend fun remove(key: String) = externalScope.runDetached { cartDao.delete(key) }

    fun applyPromo(code: PromoCode?) {
        promo.value = code
    }

    /** Called by [OrderRepository] inside the transaction that places an order. */
    suspend fun clear() {
        cartDao.clear()
        promo.value = null
    }

    private fun buildCart(lines: List<CartLineEntity>, items: List<MenuItemEntity>, promo: PromoCode?): Cart {
        val byId = items.associateBy { it.id }
        return Cart(
            lines = lines.mapNotNull { line -> byId[line.menuItemId]?.let { line.toModel(it.toModel(), extras) } },
            promo = promo
        )
    }
}
