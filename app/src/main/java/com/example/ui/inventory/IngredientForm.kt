package com.example.ui.inventory

import com.example.model.Ingredient
import com.example.model.StockUnit
import com.example.model.formatAmount
import com.example.ui.editor.toDecimal

data class IngredientForm(
    val name: String = "",
    val unit: StockUnit = StockUnit.GRAM,
    /** Starting stock. Only used when the ingredient is created; later the stock changes through transactions. */
    val stock: String = "",
    val lowThreshold: String = ""
) {
    fun validate(isNew: Boolean, otherNames: Set<String>): IngredientErrors {
        val trimmed = name.trim()
        val stockValue = stock.toDecimal()
        val thresholdValue = lowThreshold.toDecimal()
        return IngredientErrors(
            name = when {
                trimmed.length < 2 -> "Название должно быть не короче 2 символов"
                trimmed.length > MAX_NAME -> "Не длиннее $MAX_NAME символов"
                trimmed.lowercase() in otherNames -> "Такой ингредиент уже есть"
                else -> null
            },
            stock = when {
                !isNew -> null
                stockValue == null -> "Введите число"
                stockValue < 0 || stockValue > MAX_AMOUNT -> "От 0 до ${MAX_AMOUNT.formatAmount()}"
                unit == StockUnit.PIECE && stockValue % 1.0 != 0.0 -> "Штуки должны быть целым числом"
                else -> null
            },
            lowThreshold = when {
                thresholdValue == null -> "Введите число"
                thresholdValue < 0 || thresholdValue > MAX_AMOUNT -> "От 0 до ${MAX_AMOUNT.formatAmount()}"
                else -> null
            }
        )
    }

    fun toIngredient(id: Int) = Ingredient(
        id = id,
        name = name.trim(),
        unit = unit,
        stock = stock.toDecimal() ?: 0.0,
        lowThreshold = lowThreshold.toDecimal() ?: 0.0
    )

    companion object {
        const val MAX_NAME = 30
        const val MAX_AMOUNT = 1_000_000.0

        fun from(ingredient: Ingredient) = IngredientForm(
            name = ingredient.name,
            unit = ingredient.unit,
            stock = ingredient.stock.formatAmount(),
            lowThreshold = ingredient.lowThreshold.formatAmount()
        )
    }
}

data class IngredientErrors(
    val name: String? = null,
    val stock: String? = null,
    val lowThreshold: String? = null
) {
    val isValid: Boolean
        get() = listOfNotNull(name, stock, lowThreshold).isEmpty()
}

enum class StockOperation(val title: String, val action: String) {
    RESTOCK("Поставка", "Пополнить"),
    WRITE_OFF("Списание", "Списать")
}

/** Checks the amount typed in the restock / write-off dialog. Returns an error message or null. */
fun validateStockAmount(text: String, operation: StockOperation, ingredient: Ingredient, note: String): StockOperationErrors {
    val amount = text.toDecimal()
    return StockOperationErrors(
        amount = when {
            amount == null -> "Введите количество"
            amount <= 0 -> "Количество должно быть больше 0"
            amount > IngredientForm.MAX_AMOUNT -> "Слишком большое количество"
            ingredient.unit == StockUnit.PIECE && amount % 1.0 != 0.0 -> "Штуки должны быть целым числом"
            operation == StockOperation.WRITE_OFF && amount > ingredient.stock + Ingredient.EPSILON ->
                "На складе только ${ingredient.format(ingredient.stock)}"
            else -> null
        },
        note = "Укажите причину списания".takeIf { operation == StockOperation.WRITE_OFF && note.isBlank() }
    )
}

data class StockOperationErrors(
    val amount: String? = null,
    val note: String? = null
) {
    val isValid: Boolean
        get() = amount == null && note == null
}
