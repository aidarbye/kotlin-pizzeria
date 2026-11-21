package com.example.data

import androidx.room.withTransaction
import com.example.data.local.InventoryDao
import com.example.data.local.MenuDao
import com.example.data.local.OrderDao
import com.example.data.local.OrderEntity
import com.example.data.local.PizzeriaDatabase
import com.example.data.local.StockTransactionEntity
import com.example.data.local.toColumns
import com.example.data.local.toModel
import com.example.data.local.toOrderLineEntity
import com.example.model.Customer
import com.example.model.DeliveryType
import com.example.model.Extra
import com.example.model.Order
import com.example.model.OrderLine
import com.example.model.OrderStatus
import com.example.model.Pizza
import com.example.model.Shortage
import com.example.model.TransactionType
import com.example.service.Pizzeria
import com.example.service.StockCalculator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

sealed interface PlaceOrderResult {
    data class Success(val orderId: Int) : PlaceOrderResult
    data object EmptyCart : PlaceOrderResult
    data class NotEnoughStock(val shortages: List<Shortage>) : PlaceOrderResult
}

data class RepeatResult(val added: Int, val unavailable: List<String>)

class OrderRepository(
    private val database: PizzeriaDatabase,
    private val orderDao: OrderDao,
    private val menuDao: MenuDao,
    private val inventoryDao: InventoryDao,
    private val cartRepository: CartRepository,
    private val pizzeria: Pizzeria,
    private val extras: List<Extra>,
    private val scope: CoroutineScope
) {
    val orders: Flow<List<Order>> = orderDao.observeOrders().map { list -> list.map { it.toModel(extras) } }

    private val jobs = ConcurrentHashMap<Int, Job>()

    init {
        // Orders that were in progress when the app was closed continue from their saved status.
        scope.launch {
            orderDao.getActiveOrders().forEach { startProcessing(it.toModel(extras)) }
        }
    }

    fun order(id: Int): Flow<Order?> = orderDao.observeOrder(id).map { it?.toModel(extras) }

    /**
     * Places an order from the current cart in one transaction: checks the stock, saves the order,
     * takes the ingredients from the stock (one transaction per ingredient) and empties the cart.
     */
    suspend fun placeOrder(customer: Customer, deliveryType: DeliveryType, comment: String): PlaceOrderResult =
        scope.runDetached {
            val result = database.withTransaction {
                val cart = cartRepository.currentCart()
                if (cart.lines.isEmpty()) return@withTransaction PlaceOrderResult.EmptyCart

                val recipes = menuDao.getRecipes().map { it.toModel() }.groupBy { it.menuItemId }
                val ingredients = inventoryDao.getIngredients().map { it.toModel() }
                val required = StockCalculator.requirements(cart.lines, recipes)
                val shortages = StockCalculator.shortages(required, ingredients)
                if (shortages.isNotEmpty()) return@withTransaction PlaceOrderResult.NotEnoughStock(shortages)

                val quote = pizzeria.quote(cart.lines, cart.promo, deliveryType)
                val (statusType, statusValue, reason) = OrderStatus.Received.toColumns()
                val orderId = orderDao.insertOrder(
                    OrderEntity(
                        customerName = customer.name,
                        phone = customer.phone,
                        address = customer.address,
                        deliveryType = deliveryType,
                        subtotal = quote.subtotal,
                        discount = quote.discount,
                        deliveryFee = quote.deliveryFee,
                        promoCode = cart.promo?.code,
                        comment = comment,
                        createdAt = System.currentTimeMillis(),
                        statusType = statusType,
                        statusValue = statusValue,
                        cancelReason = reason
                    )
                ).toInt()
                orderDao.insertLines(cart.lines.map { it.toOrderLineEntity(orderId) })
                required.forEach { (ingredientId, amount) ->
                    inventoryDao.apply(
                        StockTransactionEntity(
                            ingredientId = ingredientId,
                            type = TransactionType.ORDER,
                            delta = -amount,
                            orderId = orderId,
                            note = "Заказ #$orderId"
                        )
                    )
                }
                cartRepository.clear()
                PlaceOrderResult.Success(orderId)
            }
            if (result is PlaceOrderResult.Success) {
                orderDao.getOrder(result.orderId)?.let { startProcessing(it.toModel(extras)) }
            }
            result
        }

    /** Cancels the order and returns its ingredients to the stock. Returns false if it is too late. */
    suspend fun cancel(orderId: Int): Boolean = scope.runDetached {
        val order = orderDao.getOrder(orderId)?.toModel(extras) ?: return@runDetached false
        if (!order.canCancel) return@runDetached false
        jobs.remove(orderId)?.cancelAndJoin()

        val cancelled = database.withTransaction {
            // The status could change while the processing coroutine was stopping.
            val current = orderDao.getOrder(orderId)?.toModel(extras)
            if (current == null || !current.canCancel) return@withTransaction false
            val (type, value, reason) = OrderStatus.Cancelled("клиентом").toColumns()
            orderDao.updateStatus(orderId, type, value, reason)
            inventoryDao.getOrderTransactions(orderId, TransactionType.ORDER).forEach { taken ->
                inventoryDao.apply(
                    StockTransactionEntity(
                        ingredientId = taken.ingredientId,
                        type = TransactionType.RETURN,
                        delta = -taken.delta,
                        orderId = orderId,
                        note = "Отмена заказа #$orderId"
                    )
                )
            }
            true
        }
        if (!cancelled) orderDao.getOrder(orderId)?.let { startProcessing(it.toModel(extras)) }
        cancelled
    }

    /** Puts the dishes of an old order back into the cart, using their current menu version. */
    suspend fun repeat(orderId: Int): RepeatResult = scope.runDetached {
        val order = orderDao.getOrder(orderId)?.toModel(extras) ?: return@runDetached RepeatResult(0, emptyList())
        val unavailable = mutableListOf<String>()
        val lines = order.lines.mapNotNull { line ->
            val current = menuDao.getById(line.item.id)?.toModel()
            if (current == null) {
                unavailable += line.item.name
                null
            } else {
                OrderLine(current, line.quantity, line.size.takeIf { current is Pizza }, line.extras.takeIf { current is Pizza }.orEmpty())
            }
        }
        cartRepository.addAll(lines)
        RepeatResult(added = lines.sumOf { it.quantity }, unavailable = unavailable.distinct())
    }

    private fun startProcessing(order: Order) {
        if (!order.isActive || jobs.containsKey(order.id)) return
        val job = scope.launch(start = CoroutineStart.LAZY) {
            pizzeria.processOrder(order) { status ->
                val (type, value, reason) = status.toColumns()
                orderDao.updateStatus(order.id, type, value, reason)
            }
            jobs.remove(order.id)
        }
        jobs[order.id] = job
        job.start()
    }
}
