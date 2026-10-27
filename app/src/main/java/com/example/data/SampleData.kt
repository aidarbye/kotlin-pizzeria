package com.example.data

import com.example.model.Dessert
import com.example.model.Drink
import com.example.model.Extra
import com.example.model.MenuItem
import com.example.model.Pizza
import com.example.model.PromoCode

object SampleData {
    val menu: List<MenuItem> = listOf(
        Pizza(1, "Margherita", 8.5, "Классика из Неаполя: томатный соус, моцарелла и свежий базилик.", setOf("томаты", "моцарелла", "базилик"), isVegetarian = true),
        Pizza(2, "Pepperoni", 10.0, "Острая колбаска пепперони и много тянущегося сыра.", setOf("пепперони", "моцарелла"), isVegetarian = false, isSpicy = true),
        Pizza(3, "Четыре сыра", 11.0, "Для любителей сыра: сливочная основа и четыре вида сыра.", setOf("моцарелла", "горгонзола", "пармезан", "чеддер"), isVegetarian = true),
        Pizza(4, "Барбекю", 12.5, "Курица, красный лук и дымный соус барбекю.", setOf("курица", "лук", "соус барбекю", "моцарелла"), isVegetarian = false),
        Pizza(5, "Овощная", 9.0, "Сладкий перец, шампиньоны, томаты и оливки.", setOf("перец", "грибы", "томаты", "оливки"), isVegetarian = true),
        Pizza(6, "Гавайская", 10.5, "Ветчина и ананас - спорно, но вкусно.", setOf("ветчина", "ананас", "моцарелла"), isVegetarian = false),
        Pizza(7, "Диабло", 11.5, "Салями, халапеньо и соус чили. Только для смелых.", setOf("салями", "халапеньо", "чили", "моцарелла"), isVegetarian = false, isSpicy = true),
        Drink(8, "Cola", 2.0, "Холодная газировка.", 500),
        Drink(9, "Апельсиновый сок", 2.5, "Свежевыжатый сок.", 330),
        Drink(10, "Лимонад", 2.2, "Домашний лимонад с мятой.", 400),
        Dessert(11, "Тирамису", 4.5, "Маскарпоне, савоярди и кофе.", isSugarFree = false),
        Dessert(12, "Чизкейк", 4.0, "Нью-Йорк чизкейк с ягодным соусом.", isSugarFree = false),
        Dessert(13, "Фруктовый салат", 3.5, "Сезонные фрукты без добавленного сахара.", isSugarFree = true)
    )

    val extras: List<Extra> = listOf(
        Extra(1, "Двойной сыр", 1.5),
        Extra(2, "Шампиньоны", 1.0),
        Extra(3, "Халапеньо", 0.8),
        Extra(4, "Бекон", 1.8),
        Extra(5, "Сырный бортик", 2.0)
    )

    val promoCodes: List<PromoCode> = listOf(
        PromoCode("STUDENT10", "Скидка 10% для студентов", { subtotal -> subtotal * 0.1 }),
        PromoCode("ROMA5", "Минус \$5 при заказе от \$25", { subtotal -> if (subtotal >= 25.0) 5.0 else 0.0 }),
        PromoCode("FREESHIP", "Бесплатная доставка", { 0.0 }, freeDelivery = true)
    )
}
