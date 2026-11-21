package com.example.ui

import com.example.model.DishType
import com.example.model.Drink
import com.example.model.Ingredient
import com.example.model.Pizza
import com.example.model.StockUnit
import com.example.ui.editor.MenuItemForm
import com.example.ui.editor.RecipeInput
import com.example.ui.inventory.IngredientForm
import com.example.ui.inventory.StockOperation
import com.example.ui.inventory.validateStockAmount
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FormValidationTest {

    private val validPizza = MenuItemForm(
        type = DishType.PIZZA,
        name = "Карбонара",
        price = "11,5",
        toppings = "бекон, сливки",
        recipe = listOf(RecipeInput(1, "250"))
    )

    @Test
    fun validPizzaFormBuildsPizzaAndRecipe() {
        assertTrue(validPizza.validate(emptySet()).isValid)
        val item = validPizza.toMenuItem(id = 7) as Pizza
        assertEquals(11.5, item.price, 1e-9)
        assertEquals(setOf("бекон", "сливки"), item.toppings)
        assertEquals(250.0, validPizza.toRecipe(7).single().amount, 1e-9)
    }

    @Test
    fun duplicateNameIsRejectedIgnoringCase() {
        val errors = validPizza.validate(setOf("карбонара"))
        assertNotNull(errors.name)
        assertFalse(errors.isValid)
    }

    @Test
    fun wrongPriceToppingsAndAmountAreRejected() {
        val errors = validPizza.copy(price = "0", toppings = " , ", recipe = listOf(RecipeInput(1, "-3"))).validate(emptySet())
        assertNotNull(errors.price)
        assertNotNull(errors.toppings)
        assertNotNull(errors.recipe[1])
    }

    @Test
    fun drinkNeedsVolume() {
        val drink = MenuItemForm(type = DishType.DRINK, name = "Морс", price = "2")
        assertNotNull(drink.validate(emptySet()).volume)
        val fixed = drink.copy(volumeMl = "400")
        assertTrue(fixed.validate(emptySet()).isValid)
        assertEquals(400, (fixed.toMenuItem(0) as Drink).volumeMl)
    }

    @Test
    fun ingredientFormChecksStockOnlyWhenCreating() {
        val form = IngredientForm(name = "Сливки", unit = StockUnit.PIECE, stock = "2.5", lowThreshold = "1")
        assertNotNull(form.validate(isNew = true, otherNames = emptySet()).stock)
        assertNull(form.validate(isNew = false, otherNames = emptySet()).stock)
    }

    @Test
    fun writeOffCannotExceedStockAndNeedsReason() {
        val cheese = Ingredient(1, "Моцарелла", StockUnit.GRAM, 300.0, 100.0)
        val tooMuch = validateStockAmount("400", StockOperation.WRITE_OFF, cheese, "")
        assertNotNull(tooMuch.amount)
        assertNotNull(tooMuch.note)
        assertTrue(validateStockAmount("300", StockOperation.WRITE_OFF, cheese, "испортилась").isValid)
        assertTrue(validateStockAmount("400", StockOperation.RESTOCK, cheese, "").isValid)
    }
}
