package com.example.model

enum class DishType(val title: String) {
    PIZZA("Пицца"),
    DRINK("Напиток"),
    DESSERT("Десерт")
}

val MenuItem.dishType: DishType
    get() = when (this) {
        is Drink -> DishType.DRINK
        is Dessert -> DishType.DESSERT
        else -> DishType.PIZZA
    }
