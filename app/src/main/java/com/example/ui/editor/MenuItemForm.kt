package com.example.ui.editor

import com.example.model.Dessert
import com.example.model.DishType
import com.example.model.Drink
import com.example.model.MenuItem
import com.example.model.Pizza
import com.example.model.RecipeEntry
import com.example.model.dishType
import com.example.model.formatAmount
import com.example.model.formatPrice

data class RecipeInput(
    val ingredientId: Int,
    val amount: String
)

/** What the user typed in the dish form. Numbers are kept as text, so half-typed values are not lost. */
data class MenuItemForm(
    val type: DishType = DishType.PIZZA,
    val name: String = "",
    val price: String = "",
    val description: String = "",
    val toppings: String = "",
    val isVegetarian: Boolean = false,
    val isSpicy: Boolean = false,
    val volumeMl: String = "",
    val isSugarFree: Boolean = false,
    val recipe: List<RecipeInput> = emptyList()
) {
    fun validate(otherNames: Set<String>): MenuItemErrors {
        val trimmedName = name.trim()
        val priceValue = price.toDecimal()
        val volumeValue = volumeMl.trim().toIntOrNull()
        return MenuItemErrors(
            name = when {
                trimmedName.length < 2 -> "Название должно быть не короче 2 символов"
                trimmedName.length > MAX_NAME -> "Не длиннее $MAX_NAME символов"
                trimmedName.lowercase() in otherNames -> "Блюдо с таким названием уже есть"
                else -> null
            },
            price = when {
                priceValue == null -> "Введите цену, например 9.50"
                priceValue < MIN_PRICE || priceValue > MAX_PRICE ->
                    "Цена от ${MIN_PRICE.formatPrice()} до ${MAX_PRICE.formatPrice()}"
                else -> null
            },
            description = "Не длиннее $MAX_DESCRIPTION символов".takeIf { description.trim().length > MAX_DESCRIPTION },
            toppings = "Укажите хотя бы одну начинку".takeIf { type == DishType.PIZZA && toppingSet().isEmpty() },
            volume = "Объём от 50 до 3000 мл".takeIf {
                type == DishType.DRINK && (volumeValue == null || volumeValue !in 50..3000)
            },
            recipe = recipe.mapNotNull { input ->
                val amount = input.amount.toDecimal()
                if (amount == null || amount <= 0.0 || amount > MAX_AMOUNT) {
                    input.ingredientId to "Больше 0 и не больше ${MAX_AMOUNT.formatAmount()}"
                } else {
                    null
                }
            }.toMap()
        )
    }

    fun toMenuItem(id: Int): MenuItem {
        val name = name.trim()
        val price = price.toDecimal() ?: 0.0
        val description = description.trim()
        return when (type) {
            DishType.PIZZA -> Pizza(id, name, price, description, toppingSet(), isVegetarian, isSpicy)
            DishType.DRINK -> Drink(id, name, price, description, volumeMl.trim().toIntOrNull() ?: 0)
            DishType.DESSERT -> Dessert(id, name, price, description, isSugarFree)
        }
    }

    fun toRecipe(menuItemId: Int): List<RecipeEntry> =
        recipe.map { RecipeEntry(menuItemId, it.ingredientId, it.amount.toDecimal() ?: 0.0) }

    private fun toppingSet(): Set<String> = toppings.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toSet()

    companion object {
        const val MAX_NAME = 40
        const val MAX_DESCRIPTION = 200
        const val MIN_PRICE = 0.5
        const val MAX_PRICE = 500.0
        const val MAX_AMOUNT = 10_000.0

        fun from(item: MenuItem, recipe: List<RecipeEntry>) = MenuItemForm(
            type = item.dishType,
            name = item.name,
            price = item.price.toString(),
            description = item.description,
            toppings = (item as? Pizza)?.toppings?.joinToString(", ").orEmpty(),
            isVegetarian = (item as? Pizza)?.isVegetarian ?: false,
            isSpicy = (item as? Pizza)?.isSpicy ?: false,
            volumeMl = (item as? Drink)?.volumeMl?.toString().orEmpty(),
            isSugarFree = (item as? Dessert)?.isSugarFree ?: false,
            recipe = recipe.map { RecipeInput(it.ingredientId, it.amount.formatAmount()) }
        )
    }
}

data class MenuItemErrors(
    val name: String? = null,
    val price: String? = null,
    val description: String? = null,
    val toppings: String? = null,
    val volume: String? = null,
    /** Errors of recipe amounts, by ingredient id. */
    val recipe: Map<Int, String> = emptyMap()
) {
    val isValid: Boolean
        get() = listOfNotNull(name, price, description, toppings, volume).isEmpty() && recipe.isEmpty()
}

/** Accepts both "9.5" and "9,5". */
fun String.toDecimal(): Double? = trim().replace(',', '.').toDoubleOrNull()
