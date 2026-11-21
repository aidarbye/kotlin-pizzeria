package com.example.data

import com.example.model.Dessert
import com.example.model.Drink
import com.example.model.Extra
import com.example.model.Ingredient
import com.example.model.MenuItem
import com.example.model.Pizza
import com.example.model.PromoCode
import com.example.model.RecipeEntry
import com.example.model.StockUnit

/** Data the database is filled with on the first launch, and static data that is not edited by the user. */
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

    val ingredients: List<Ingredient> = listOf(
        Ingredient(1, "Тесто", StockUnit.GRAM, 5000.0, 1000.0),
        Ingredient(2, "Томатный соус", StockUnit.MILLILITER, 3000.0, 500.0),
        Ingredient(3, "Моцарелла", StockUnit.GRAM, 4000.0, 800.0),
        Ingredient(4, "Базилик", StockUnit.GRAM, 200.0, 50.0),
        Ingredient(5, "Пепперони", StockUnit.GRAM, 1200.0, 300.0),
        Ingredient(6, "Горгонзола", StockUnit.GRAM, 600.0, 150.0),
        Ingredient(7, "Пармезан", StockUnit.GRAM, 600.0, 150.0),
        Ingredient(8, "Чеддер", StockUnit.GRAM, 600.0, 150.0),
        Ingredient(9, "Курица", StockUnit.GRAM, 1500.0, 300.0),
        Ingredient(10, "Соус барбекю", StockUnit.MILLILITER, 800.0, 200.0),
        Ingredient(11, "Красный лук", StockUnit.GRAM, 1000.0, 200.0),
        Ingredient(12, "Сладкий перец", StockUnit.GRAM, 800.0, 200.0),
        Ingredient(13, "Шампиньоны", StockUnit.GRAM, 800.0, 200.0),
        Ingredient(14, "Оливки", StockUnit.GRAM, 500.0, 100.0),
        Ingredient(15, "Ветчина", StockUnit.GRAM, 1200.0, 300.0),
        Ingredient(16, "Ананас", StockUnit.GRAM, 600.0, 150.0),
        Ingredient(17, "Салями", StockUnit.GRAM, 400.0, 300.0),
        Ingredient(18, "Халапеньо", StockUnit.GRAM, 300.0, 80.0),
        Ingredient(19, "Томаты", StockUnit.GRAM, 2000.0, 400.0),
        Ingredient(20, "Cola 0.5 л", StockUnit.PIECE, 24.0, 6.0),
        Ingredient(21, "Апельсины", StockUnit.PIECE, 30.0, 10.0),
        Ingredient(22, "Лимоны", StockUnit.PIECE, 6.0, 5.0),
        Ingredient(23, "Маскарпоне", StockUnit.GRAM, 1000.0, 250.0),
        Ingredient(24, "Савоярди", StockUnit.PIECE, 40.0, 10.0),
        Ingredient(25, "Сливочный сыр", StockUnit.GRAM, 1500.0, 300.0),
        Ingredient(26, "Сезонные фрукты", StockUnit.GRAM, 0.0, 500.0)
    )

    /** menuItemId to (ingredientId to amount per portion). */
    val recipes: List<RecipeEntry> = mapOf(
        1 to listOf(1 to 250.0, 2 to 80.0, 3 to 120.0, 19 to 60.0, 4 to 5.0),
        2 to listOf(1 to 250.0, 2 to 80.0, 3 to 130.0, 5 to 90.0),
        3 to listOf(1 to 250.0, 3 to 80.0, 6 to 50.0, 7 to 40.0, 8 to 50.0),
        4 to listOf(1 to 250.0, 3 to 110.0, 9 to 120.0, 11 to 40.0, 10 to 60.0),
        5 to listOf(1 to 250.0, 2 to 80.0, 3 to 100.0, 12 to 60.0, 13 to 60.0, 19 to 60.0, 14 to 30.0),
        6 to listOf(1 to 250.0, 2 to 80.0, 3 to 110.0, 15 to 100.0, 16 to 70.0),
        7 to listOf(1 to 250.0, 2 to 80.0, 3 to 110.0, 17 to 90.0, 18 to 30.0),
        8 to listOf(20 to 1.0),
        9 to listOf(21 to 3.0),
        10 to listOf(22 to 2.0),
        11 to listOf(23 to 100.0, 24 to 4.0),
        12 to listOf(25 to 120.0),
        13 to listOf(26 to 200.0)
    ).flatMap { (menuItemId, entries) ->
        entries.map { (ingredientId, amount) -> RecipeEntry(menuItemId, ingredientId, amount) }
    }
}
