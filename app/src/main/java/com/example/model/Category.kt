package com.example.model

enum class Category(val title: String) {
    ALL("Всё"),
    PIZZA("Пицца"),
    DRINK("Напитки"),
    DESSERT("Десерты");

    fun matches(item: MenuItem): Boolean = when (this) {
        ALL -> true
        PIZZA -> item is Pizza
        DRINK -> item is Drink
        DESSERT -> item is Dessert
    }
}
