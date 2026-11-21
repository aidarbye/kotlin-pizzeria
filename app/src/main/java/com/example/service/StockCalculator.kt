package com.example.service

import com.example.model.Ingredient
import com.example.model.OrderLine
import com.example.model.PizzaSize
import com.example.model.RecipeEntry
import com.example.model.Shortage
import com.example.model.portionFactor
import kotlin.math.floor

/** Plain Kotlin logic of the stock: how much the cart needs, what is missing, how many portions are left. */
object StockCalculator {

    fun requirements(lines: List<OrderLine>, recipes: Map<Int, List<RecipeEntry>>): Map<Int, Double> =
        lines.flatMap { line ->
            recipes[line.item.id].orEmpty().map { entry ->
                entry.ingredientId to entry.amount * line.size.portionFactor() * line.quantity
            }
        }
            .groupBy({ it.first }, { it.second })
            .mapValues { (_, amounts) -> amounts.sum() }

    fun shortages(required: Map<Int, Double>, ingredients: List<Ingredient>): List<Shortage> {
        val byId = ingredients.associateBy { it.id }
        return required.mapNotNull { (id, amount) ->
            val ingredient = byId[id] ?: return@mapNotNull null
            if (amount > ingredient.stock + Ingredient.EPSILON) Shortage(ingredient, amount, ingredient.stock) else null
        }.sortedBy { it.ingredient.name }
    }

    /** Stock that stays free after the given requirements are reserved. */
    fun remaining(ingredients: List<Ingredient>, reserved: Map<Int, Double>): Map<Int, Double> =
        ingredients.associate { it.id to (it.stock - (reserved[it.id] ?: 0.0)).coerceAtLeast(0.0) }

    /** How many more portions can be made, or null when the dish does not use tracked ingredients. */
    fun portionsLeft(recipe: List<RecipeEntry>, size: PizzaSize?, remaining: Map<Int, Double>): Int? {
        if (recipe.isEmpty()) return null
        return recipe.minOf { entry ->
            val perPortion = entry.amount * size.portionFactor()
            val free = remaining[entry.ingredientId] ?: 0.0
            if (perPortion <= 0.0) Int.MAX_VALUE else floor((free + Ingredient.EPSILON) / perPortion).toInt()
        }
    }
}
