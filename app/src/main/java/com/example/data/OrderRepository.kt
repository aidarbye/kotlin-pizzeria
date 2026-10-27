package com.example.data

import com.example.model.Customer
import com.example.model.DeliveryType
import com.example.model.Order
import com.example.model.OrderStatus
import com.example.service.Pizzeria
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

class OrderRepository(
    private val pizzeria: Pizzeria,
    private val cartRepository: CartRepository,
    private val scope: CoroutineScope
) {
    private val _orders = MutableStateFlow<List<Order>>(emptyList())
    val orders: StateFlow<List<Order>> = _orders.asStateFlow()

    private val jobs = ConcurrentHashMap<Int, Job>()
    private val nextId = AtomicInteger(1)

    fun placeOrder(customer: Customer, deliveryType: DeliveryType, comment: String): Order? {
        val cart = cartRepository.cart.value
        if (cart.lines.isEmpty()) return null

        val order = Order(
            id = nextId.getAndIncrement(),
            customer = customer,
            lines = cart.lines,
            deliveryType = deliveryType,
            quote = pizzeria.quote(cart.lines, cart.promo, deliveryType),
            promoCode = cart.promo?.code,
            comment = comment
        )
        _orders.update { listOf(order) + it }
        cartRepository.clear()

        jobs[order.id] = scope.launch {
            pizzeria.processOrder(order) { status -> updateStatus(order.id, status) }
            jobs.remove(order.id)
        }
        return order
    }

    fun cancel(orderId: Int) {
        val order = _orders.value.find { it.id == orderId } ?: return
        if (!order.canCancel) return
        jobs.remove(orderId)?.cancel()
        updateStatus(orderId, OrderStatus.Cancelled("клиентом"))
    }

    fun repeat(orderId: Int) {
        val order = _orders.value.find { it.id == orderId } ?: return
        cartRepository.addAll(order.lines)
    }

    private fun updateStatus(orderId: Int, status: OrderStatus) = _orders.update { orders ->
        orders.map { if (it.id == orderId) it.copy(status = status) else it }
    }
}
