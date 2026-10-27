package com.example.service

import com.example.contract.Preparable
import com.example.model.Category
import com.example.model.DeliveryType
import com.example.model.MenuItem
import com.example.model.Order
import com.example.model.OrderLine
import com.example.model.OrderStatus
import com.example.model.Pizza
import com.example.model.PriceQuote
import com.example.model.PromoCode
import com.example.model.isVegetarian
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

class Pizzeria(
    val name: String,
    val menu: List<MenuItem>,
    private val promoCodes: List<PromoCode>,
    private val timeScale: Long = 3L
) {
    fun filterMenu(predicate: (MenuItem) -> Boolean): List<MenuItem> = menu.filter(predicate)

    fun search(
        query: String,
        category: Category,
        vegetarianOnly: Boolean,
        favoriteIds: Set<Int>? = null
    ): List<MenuItem> {
        val text = query.trim()
        return filterMenu { item ->
            category.matches(item) &&
                (!vegetarianOnly || item.isVegetarian) &&
                (favoriteIds == null || item.id in favoriteIds) &&
                (text.isEmpty() ||
                    item.name.contains(text, ignoreCase = true) ||
                    (item is Pizza && item.toppings.any { it.contains(text, ignoreCase = true) }))
        }
    }

    fun findItem(id: Int): MenuItem? = menu.find { it.id == id }

    fun findPromo(code: String): PromoCode? = promoCodes.find { it.code.equals(code.trim(), ignoreCase = true) }

    fun quote(lines: List<OrderLine>, promo: PromoCode?, deliveryType: DeliveryType): PriceQuote {
        val subtotal = lines.sumOf { it.subtotal }
        val deliveryFee = when {
            deliveryType == DeliveryType.PICKUP -> 0.0
            promo?.freeDelivery == true -> 0.0
            subtotal >= FREE_DELIVERY_FROM -> 0.0
            else -> DELIVERY_FEE
        }
        return PriceQuote(subtotal, promo?.discountFor(subtotal) ?: 0.0, deliveryFee)
    }

    fun totalRevenue(orders: List<Order>): Double =
        orders.filter { it.status !is OrderStatus.Cancelled }
            .map { it.quote.total }
            .reduceOrNull { acc, sum -> acc + sum } ?: 0.0

    fun popularity(orders: List<Order>): Map<String, Int> =
        orders.filter { it.status !is OrderStatus.Cancelled }
            .flatMap { it.lines }
            .groupBy { it.item.name }
            .mapValues { (_, lines) -> lines.sumOf { it.quantity } }

    suspend fun processOrder(order: Order, onStatus: (OrderStatus) -> Unit) {
        if (order.lines.isEmpty()) {
            onStatus(OrderStatus.Cancelled("пустой заказ"))
            return
        }

        onStatus(OrderStatus.Received)
        delay((500 * timeScale).milliseconds)

        val cookTime = order.lines.sumOf { line ->
            (line.item as? Preparable)?.prepareTimeMs()?.times(line.quantity) ?: 0L
        }.coerceIn(1000L, 4000L) * timeScale
        for (progress in 10..100 step 10) {
            delay((cookTime / 10).milliseconds)
            onStatus(OrderStatus.Cooking(progress))
        }

        if (order.deliveryType == DeliveryType.DELIVERY) {
            onStatus(OrderStatus.Delivering)
            delay((1500 * timeScale).milliseconds)
        }
        onStatus(OrderStatus.Delivered(minutes = 15 + order.lines.size * 5))
    }

    companion object {
        const val DELIVERY_FEE = 2.5
        const val FREE_DELIVERY_FROM = 30.0
    }
}
