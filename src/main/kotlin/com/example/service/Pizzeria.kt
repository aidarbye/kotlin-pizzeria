package com.example.service

import com.example.model.MenuItem
import com.example.model.Order
import com.example.model.OrderStatus
import com.example.model.toText
import com.example.model.Pizza
import com.example.contract.Preparable
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

class Pizzeria(
    val name: String, private val menu: List<MenuItem>
) {
    private val orders = mutableListOf<Order>()

    fun addOrder(order: Order) {
        orders.add(order)
    }

    fun printMenu() {
        println("=== Меню $name ===")
        for (item in menu) {
            println(item.describe())
        }
    }

    fun filterMenu(predicate: (MenuItem) -> Boolean): List<MenuItem> = menu.filter(predicate)

    fun vegetarianPizzas(): List<Pizza> = menu.filterIsInstance<Pizza>().filter { it.isVegetarian }

    fun totalWithDiscount(order: Order, rule: (Double) -> Double): Double = rule(order.total())

    fun totalRevenue(): Double = orders.map { it.total() }.reduceOrNull { acc, sum -> acc + sum } ?: 0.0

    fun ordersByStatus(): Map<String, List<Order>> = orders.groupBy { it.status::class.simpleName ?: "Unknown" }

    fun popularity(): Map<String, Int> =
        orders.flatMap { it.lines }.groupBy { it.item.name }.mapValues { (_, lines) -> lines.sumOf { it.quantity } }

    suspend fun processOrder(order: Order) {
        if (order.lines.isEmpty()) {
            updateStatus(order, OrderStatus.Cancelled("пустой заказ"))
            return
        }

        updateStatus(order, OrderStatus.Received)

        val cookTime = order.lines.sumOf { line ->
            (line.item as? Preparable)?.prepareTimeMs()?.times(line.quantity) ?: 0L
        }
        for (progress in 25..100 step 25) {
            delay((cookTime / 4).milliseconds)
            updateStatus(order, OrderStatus.Cooking(progress))
        }

        updateStatus(order, OrderStatus.Delivering)
        delay(800.milliseconds)
        updateStatus(order, OrderStatus.Delivered(minutes = 20))
    }

    private fun updateStatus(order: Order, status: OrderStatus) {
        order.status = status
        println("[Заказ #${order.id}, ${order.customer.name}] ${status.toText()}")
    }

    fun allToppings(): Set<String> = menu.filterIsInstance<Pizza>().flatMap { it.toppings }.toSet()
}
