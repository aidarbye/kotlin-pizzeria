package com.example.model

import com.example.contract.Preparable

abstract class MenuItem(
    val id: Int,
    val name: String,
    val price: Double
) {
    abstract fun details(): String

    open fun describe(): String = "$name - $price"
}

class Pizza(
    id: Int,
    name: String,
    price: Double,
    val sizeCm: Int,
    val toppings: Set<String>,
    val isVegetarian: Boolean
) : MenuItem(id, name, price), Preparable {

    override fun details(): String = "$sizeCm см · ${toppings.joinToString()}"

    override fun describe(): String =
        "Пицца $name ($sizeCm см), начинка: ${toppings.joinToString()} - $price"

    override fun prepareTimeMs(): Long = 500L + sizeCm * 10L + toppings.size * 100L
}

class Drink(
    id: Int,
    name: String,
    price: Double,
    val volumeMl: Int
) : MenuItem(id, name, price) {

    override fun details(): String = "$volumeMl мл"

    override fun describe(): String = "Напиток $name ($volumeMl мл) - $price"
}

class Dessert(
    id: Int,
    name: String,
    price: Double,
    val isSugarFree: Boolean
) : MenuItem(id, name, price), Preparable {

    override fun details(): String = if (isSugarFree) "без сахара" else "с сахаром"

    override fun describe(): String = "Десерт $name (${details()}) - $price"

    override fun prepareTimeMs(): Long = 300L
}

val MenuItem.isVegetarian: Boolean
    get() = this !is Pizza || this.isVegetarian

fun Double.formatPrice(): String = String.format(java.util.Locale.US, "\$%.2f", this)
