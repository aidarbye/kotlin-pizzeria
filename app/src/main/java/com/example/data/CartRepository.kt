package com.example.data

import com.example.model.Cart
import com.example.model.OrderLine
import com.example.model.PromoCode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class CartRepository {

    private val _cart = MutableStateFlow(Cart())
    val cart: StateFlow<Cart> = _cart.asStateFlow()

    fun add(line: OrderLine) = _cart.update { cart ->
        val existing = cart.lines.find { it.key == line.key }
        val lines = if (existing == null) {
            cart.lines + line
        } else {
            cart.lines.map { if (it.key == line.key) it.copy(quantity = it.quantity + line.quantity) else it }
        }
        cart.copy(lines = lines)
    }

    fun addAll(lines: List<OrderLine>) = lines.forEach(::add)

    fun changeQuantity(key: String, delta: Int) = _cart.update { cart ->
        cart.copy(
            lines = cart.lines
                .map { if (it.key == key) it.copy(quantity = it.quantity + delta) else it }
                .filter { it.quantity > 0 }
        )
    }

    fun removeOneOf(itemId: Int) {
        val line = _cart.value.lines.lastOrNull { it.item.id == itemId } ?: return
        changeQuantity(line.key, -1)
    }

    fun remove(key: String) = _cart.update { cart -> cart.copy(lines = cart.lines.filterNot { it.key == key }) }

    fun applyPromo(promo: PromoCode?) = _cart.update { it.copy(promo = promo) }

    fun clear() = _cart.update { Cart() }
}
