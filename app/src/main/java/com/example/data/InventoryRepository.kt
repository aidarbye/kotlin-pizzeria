package com.example.data

import androidx.room.withTransaction
import com.example.data.local.InventoryDao
import com.example.data.local.PizzeriaDatabase
import com.example.data.local.StockTransactionEntity
import com.example.data.local.toEntity
import com.example.data.local.toModel
import com.example.model.DishUsage
import com.example.model.Ingredient
import com.example.model.StockTransaction
import com.example.model.TransactionType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class InventoryRepository(
    private val database: PizzeriaDatabase,
    private val inventoryDao: InventoryDao,
    private val externalScope: CoroutineScope
) {
    val ingredients: Flow<List<Ingredient>> = inventoryDao.observeIngredients().map { list -> list.map { it.toModel() } }

    val allTransactions: Flow<List<StockTransaction>> =
        inventoryDao.observeAllTransactions().map { list -> list.map { it.toModel() } }

    fun ingredient(id: Int): Flow<Ingredient?> = inventoryDao.observeIngredient(id).map { it?.toModel() }

    fun transactions(ingredientId: Int): Flow<List<StockTransaction>> =
        inventoryDao.observeTransactions(ingredientId).map { list -> list.map { it.toModel() } }

    fun usages(ingredientId: Int): Flow<List<DishUsage>> =
        inventoryDao.observeUsages(ingredientId).map { list -> list.map { it.toModel() } }

    suspend fun getIngredient(id: Int): Ingredient? = inventoryDao.getIngredient(id)?.toModel()

    /** The starting stock is written as a "restock" transaction, so the history matches the stock. */
    suspend fun create(ingredient: Ingredient): Int = externalScope.runDetached {
        inventoryDao.createWithInitialStock(ingredient.toEntity().copy(id = 0))
    }

    /** Updates name, unit and threshold. The stock itself changes only through transactions. */
    suspend fun updateInfo(ingredient: Ingredient) = externalScope.runDetached {
        inventoryDao.updateIngredientInfo(ingredient.id, ingredient.name, ingredient.unit, ingredient.lowThreshold)
    }

    suspend fun delete(id: Int) = externalScope.runDetached { inventoryDao.deleteIngredient(id) }

    suspend fun restock(id: Int, amount: Double, note: String) = externalScope.runDetached {
        inventoryDao.apply(StockTransactionEntity(ingredientId = id, type = TransactionType.RESTOCK, delta = amount, note = note))
    }

    /** Returns false when there is less in stock than the user tries to write off. */
    suspend fun writeOff(id: Int, amount: Double, note: String): Boolean = externalScope.runDetached {
        database.withTransaction {
            val stock = inventoryDao.getIngredient(id)?.stock ?: return@withTransaction false
            if (amount > stock + Ingredient.EPSILON) return@withTransaction false
            inventoryDao.apply(
                StockTransactionEntity(ingredientId = id, type = TransactionType.WRITE_OFF, delta = -amount, note = note)
            )
            true
        }
    }
}
