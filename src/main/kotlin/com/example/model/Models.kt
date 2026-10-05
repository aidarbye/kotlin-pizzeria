package com.example.model

import com.example.contract.Preparable

abstract class MenuItem(
    val name: String,
    val price: Double
) {
    open fun describe(): String = "$name - $price"
}

class Pizza(
    name: String,
    price: Double,
    val sizeCm: Int,
    val toppings: Set<String>,
    val isVegetarian: Boolean
) : MenuItem(name, price), Preparable {

    override fun describe(): String =
        "Пицца $name ($sizeCm см), начинка: ${toppings.joinToString()} - $price"

    override fun prepareTimeMs(): Long = 500L + sizeCm * 10L + toppings.size * 100L
}

class Drink(
    name: String,
    price: Double,
    val volumeMl: Int
) : MenuItem(name, price) {

    override fun describe(): String = "Напиток $name ($volumeMl мл) - $price"
}

class Dessert(
    name: String,
    price: Double,
    val isSugarFree: Boolean
) : MenuItem(name, price), Preparable {

    override fun describe(): String {
        val sugar = if (isSugarFree) "без сахара" else "с сахаром"
        return "Десерт $name ($sugar) - $price"
    }

    override fun prepareTimeMs(): Long = 300L
}
