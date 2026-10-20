package com.example.data

import com.example.model.Dessert
import com.example.model.Drink
import com.example.model.MenuItem
import com.example.model.Pizza
import com.example.service.Pizzeria

object SampleData {
    val menu: List<MenuItem> = listOf(
        Pizza(1, "Margherita", 8.5, 30, setOf("томаты", "моцарелла", "базилик"), true),
        Pizza(2, "Pepperoni", 10.0, 35, setOf("пепперони", "моцарелла"), false),
        Pizza(3, "Четыре сыра", 11.0, 30, setOf("моцарелла", "горгонзола", "пармезан", "чеддер"), true),
        Pizza(4, "Барбекю", 12.5, 35, setOf("курица", "лук", "соус барбекю", "моцарелла"), false),
        Pizza(5, "Овощная", 9.0, 30, setOf("перец", "грибы", "томаты", "оливки"), true),
        Pizza(6, "Гавайская", 10.5, 30, setOf("ветчина", "ананас", "моцарелла"), false),
        Drink(7, "Cola", 2.0, 500),
        Drink(8, "Апельсиновый сок", 2.5, 330),
        Drink(9, "Лимонад", 2.2, 400),
        Dessert(10, "Тирамису", 4.5, false),
        Dessert(11, "Чизкейк", 4.0, false),
        Dessert(12, "Фруктовый салат", 3.5, true)
    )

    val pizzeria = Pizzeria("Roma", menu)
}
