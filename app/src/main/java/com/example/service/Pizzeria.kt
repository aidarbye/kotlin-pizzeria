package com.example.service

import com.example.contract.Preparable
import com.example.model.Category
import com.example.model.MenuItem
import com.example.model.Order
import com.example.model.OrderStatus
import com.example.model.Pizza
import com.example.model.isVegetarian
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

class Pizzeria(
    val name: String,
    val menu: List<MenuItem>,
    private val timeScale: Long = 3L
) {
    fun filterMenu(predicate: (MenuItem) -> Boolean): List<MenuItem> = menu.filter(predicate)

    fun search(query: String, category: Category, vegetarianOnly: Boolean): List<MenuItem> {
        val text = query.trim()
        return filterMenu { item ->
            category.matches(item) &&
                (!vegetarianOnly || item.isVegetarian) &&
                (text.isEmpty() ||
                    item.name.contains(text, ignoreCase = true) ||
                    (item is Pizza && item.toppings.any { it.contains(text, ignoreCase = true) }))
        }
    }

    fun findItem(id: Int): MenuItem? = menu.find { it.id == id }

    fun vegetarianPizzas(): List<Pizza> = menu.filterIsInstance<Pizza>().filter { it.isVegetarian }

    fun totalWithDiscount(order: Order, rule: (Double) -> Double): Double = rule(order.total())

    fun totalRevenue(orders: List<Order>): Double =
        orders.filter { it.status !is OrderStatus.Cancelled }
            .map { it.paid }
            .reduceOrNull { acc, sum -> acc + sum } ?: 0.0

    fun ordersByStatus(orders: List<Order>): Map<String, List<Order>> =
        orders.groupBy { it.status::class.simpleName ?: "Unknown" }

    fun popularity(orders: List<Order>): Map<String, Int> =
        orders.filter { it.status !is OrderStatus.Cancelled }
            .flatMap { it.lines }
            .groupBy { it.item.name }
            .mapValues { (_, lines) -> lines.sumOf { it.quantity } }

    fun allToppings(): Set<String> = menu.filterIsInstance<Pizza>().flatMap { it.toppings }.toSet()

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

        onStatus(OrderStatus.Delivering)
        delay((1000 * timeScale).milliseconds)
        onStatus(OrderStatus.Delivered(minutes = 15 + order.lines.size * 5))
    }
}
