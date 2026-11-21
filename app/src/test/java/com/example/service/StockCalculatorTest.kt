package com.example.service

import com.example.model.Drink
import com.example.model.Ingredient
import com.example.model.OrderLine
import com.example.model.Pizza
import com.example.model.PizzaSize
import com.example.model.RecipeEntry
import com.example.model.StockUnit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class StockCalculatorTest {

    private val dough = Ingredient(1, "Тесто", StockUnit.GRAM, 1000.0, 200.0)
    private val cheese = Ingredient(2, "Моцарелла", StockUnit.GRAM, 300.0, 100.0)
    private val pizza = Pizza(10, "Margherita", 8.5, "", setOf("сыр"), isVegetarian = true)
    private val cola = Drink(20, "Cola", 2.0, "", 500)
    private val recipes = mapOf(
        10 to listOf(RecipeEntry(10, 1, 250.0), RecipeEntry(10, 2, 100.0))
    )

    @Test
    fun requirementsScaleWithSizeAndQuantity() {
        val lines = listOf(
            OrderLine(pizza, quantity = 2, size = PizzaSize.MEDIUM),
            OrderLine(pizza, quantity = 1, size = PizzaSize.LARGE),
            OrderLine(cola, quantity = 3)
        )
        val required = StockCalculator.requirements(lines, recipes)
        assertEquals(250.0 * 2 + 250.0 * 1.3, required.getValue(1), 1e-9)
        assertEquals(100.0 * 2 + 100.0 * 1.3, required.getValue(2), 1e-9)
        assertEquals(2, required.size)
    }

    @Test
    fun shortagesListOnlyMissingIngredients() {
        val required = mapOf(1 to 500.0, 2 to 330.0)
        val shortages = StockCalculator.shortages(required, listOf(dough, cheese))
        assertEquals(1, shortages.size)
        assertEquals("Моцарелла", shortages.single().ingredient.name)
        assertEquals(30.0, shortages.single().missing, 1e-9)
    }

    @Test
    fun portionsLeftUsesTheScarcestIngredient() {
        val remaining = StockCalculator.remaining(listOf(dough, cheese), reserved = mapOf(2 to 100.0))
        // Dough is enough for 4 pizzas, but only 200 g of cheese stay free: 2 pizzas.
        assertEquals(2, StockCalculator.portionsLeft(recipes.getValue(10), PizzaSize.MEDIUM, remaining))
        assertEquals(1, StockCalculator.portionsLeft(recipes.getValue(10), PizzaSize.LARGE, remaining))
    }

    @Test
    fun dishWithoutRecipeIsNotTracked() {
        assertNull(StockCalculator.portionsLeft(emptyList(), null, emptyMap()))
    }

    @Test
    fun exactStockIsEnoughDespiteRounding() {
        val small = Ingredient(3, "Базилик", StockUnit.GRAM, 4.0, 1.0)
        val required = mapOf(3 to 5.0 * 0.8)
        assertTrue(StockCalculator.shortages(required, listOf(small)).isEmpty())
    }
}
