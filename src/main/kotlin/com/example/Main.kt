package com.example

import com.example.model.Customer
import com.example.model.Dessert
import com.example.model.Drink
import com.example.model.MenuItem
import com.example.model.Order
import com.example.model.OrderLine
import com.example.model.Pizza
import com.example.service.Pizzeria
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    val menu: List<MenuItem> = listOf(
        Pizza("Margherita", 8.5, 30, setOf("томаты", "моцарелла"), true),
        Pizza("Pepperoni", 10.0, 35, setOf("пепперони", "моцарелла"), false),
        Drink("Cola", 2.0, 500),
        Dessert("Тирамису", 4.5, false)
    )
    val pizzeria = Pizzeria("Roma", menu)
    pizzeria.printMenu()

    val orders = listOf(
        Order(1, Customer("Айдар", "ул. Абая 10"), listOf(OrderLine(menu[0], 2), OrderLine(menu[2], 1))),
        Order(2, Customer("Мария", "ул. Сатпаева 5"), listOf(OrderLine(menu[1], 1), OrderLine(menu[3], 2))),
        Order(3, Customer("Данияр", "пр. Достык 7"), emptyList())
    )
    orders.forEach { pizzeria.addOrder(it) }

    println("\n=== Обработка заказов (параллельно) ===")
    val jobs = orders.map { order ->
        launch { pizzeria.processOrder(order) }
    }
    jobs.joinAll()

    println("\n=== Статистика ===")
    println("Дешёвые позиции: " + pizzeria.filterMenu { it.price < 5 }.map { it.name })
    println("Вегетарианские: " + pizzeria.vegetarianPizzas().map { it.name })
    println("Заказ #1 со скидкой 10%: " + pizzeria.totalWithDiscount(orders[0]) { it * 0.9 })
    println("Выручка: " + pizzeria.totalRevenue())
    println("Популярность: " + pizzeria.popularity())
    println("Начинки: " + pizzeria.allToppings())
    println("По статусам: " + pizzeria.ordersByStatus().mapValues { (_, list) -> list.map { it.id } })
}
