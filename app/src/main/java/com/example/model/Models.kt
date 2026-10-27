package com.example.model

import com.example.contract.Preparable

abstract class MenuItem(
    val id: Int,
    val name: String,
    val price: Double,
    val description: String
) {
    abstract fun details(): String

    open fun describe(): String = "$name - $price"
}

class Pizza(
    id: Int,
    name: String,
    price: Double,
    description: String,
    val toppings: Set<String>,
    val isVegetarian: Boolean,
    val isSpicy: Boolean = false
) : MenuItem(id, name, price, description), Preparable {

    override fun details(): String = toppings.joinToString()

    override fun describe(): String = "Пицца $name, начинка: ${toppings.joinToString()} - $price"

    override fun prepareTimeMs(): Long = 800L + toppings.size * 100L
}

class Drink(
    id: Int,
    name: String,
    price: Double,
    description: String,
    val volumeMl: Int
) : MenuItem(id, name, price, description) {

    override fun details(): String = "$volumeMl мл"

    override fun describe(): String = "Напиток $name ($volumeMl мл) - $price"
}

class Dessert(
    id: Int,
    name: String,
    price: Double,
    description: String,
    val isSugarFree: Boolean
) : MenuItem(id, name, price, description), Preparable {

    override fun details(): String = if (isSugarFree) "без сахара" else "с сахаром"

    override fun describe(): String = "Десерт $name (${details()}) - $price"

    override fun prepareTimeMs(): Long = 300L
}

enum class PizzaSize(val cm: Int, val title: String, val priceFactor: Double) {
    SMALL(25, "Маленькая", 0.8),
    MEDIUM(30, "Средняя", 1.0),
    LARGE(35, "Большая", 1.3)
}

data class Extra(
    val id: Int,
    val name: String,
    val price: Double
)

val MenuItem.isVegetarian: Boolean
    get() = this !is Pizza || this.isVegetarian

fun MenuItem.priceFor(size: PizzaSize?, extras: Set<Extra>): Double =
    price * (size?.priceFactor ?: 1.0) + extras.sumOf { it.price }

fun Double.formatPrice(): String = String.format(java.util.Locale.US, "\$%.2f", this)
