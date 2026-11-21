package com.example.data.local

import androidx.room.withTransaction
import com.example.data.SampleData
import com.example.model.TransactionType

/** Fills a freshly created database with the starting menu, stock and recipes. */
class DatabaseSeeder(private val database: PizzeriaDatabase) {

    suspend fun seed() = database.withTransaction {
        val menuDao = database.menuDao()
        val inventoryDao = database.inventoryDao()

        menuDao.insertAll(SampleData.menu.map { it.toEntity() })
        inventoryDao.insertIngredients(SampleData.ingredients.map { it.toEntity().copy(stock = 0.0) })
        SampleData.ingredients.filter { it.stock > 0 }.forEach { ingredient ->
            inventoryDao.apply(
                StockTransactionEntity(
                    ingredientId = ingredient.id,
                    type = TransactionType.RESTOCK,
                    delta = ingredient.stock,
                    note = "Начальный остаток"
                )
            )
        }
        menuDao.insertRecipe(SampleData.recipes.map { it.toEntity() })
    }
}
