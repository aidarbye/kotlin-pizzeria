package com.example.model

import java.util.Locale

enum class StockUnit(val short: String, val title: String) {
    GRAM("г", "Граммы"),
    MILLILITER("мл", "Миллилитры"),
    PIECE("шт", "Штуки")
}

data class Ingredient(
    val id: Int,
    val name: String,
    val unit: StockUnit,
    val stock: Double,
    val lowThreshold: Double
) {
    val isOut: Boolean
        get() = stock < EPSILON

    val isLow: Boolean
        get() = !isOut && stock <= lowThreshold

    fun format(amount: Double): String = "${amount.formatAmount()} ${unit.short}"

    companion object {
        const val EPSILON = 1e-6
    }
}

/** How much of an ingredient one portion of a dish needs. For pizzas it is the amount for the medium size. */
data class RecipeEntry(
    val menuItemId: Int,
    val ingredientId: Int,
    val amount: Double
)

enum class TransactionType(val title: String) {
    RESTOCK("Поставка"),
    WRITE_OFF("Списание"),
    ORDER("Заказ"),
    RETURN("Возврат")
}

data class StockTransaction(
    val id: Long,
    val ingredientId: Int,
    val type: TransactionType,
    val delta: Double,
    val orderId: Int?,
    val note: String,
    val createdAt: Long
)

data class Shortage(
    val ingredient: Ingredient,
    val required: Double,
    val available: Double
) {
    val missing: Double
        get() = required - available

    fun describe(): String = "${ingredient.name}: нужно ${ingredient.format(required)}, есть ${ingredient.format(available)}"
}

/** A dish that uses some ingredient, with the amount per portion. */
data class DishUsage(
    val menuItemId: Int,
    val name: String,
    val type: DishType,
    val amount: Double
)

/** Amount needed for one portion of the given size (a large pizza takes more cheese than a small one). */
fun PizzaSize?.portionFactor(): Double = this?.priceFactor ?: 1.0

fun Double.formatAmount(): String {
    val rounded = Math.round(this * 10) / 10.0
    return String.format(Locale.US, if (rounded % 1.0 == 0.0) "%.0f" else "%.1f", rounded)
}
