package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.model.StockUnit
import com.example.model.TransactionType
import kotlinx.coroutines.flow.Flow

@Dao
interface MenuDao {
    @Query("SELECT * FROM menu_items ORDER BY CASE type WHEN 'PIZZA' THEN 0 WHEN 'DRINK' THEN 1 ELSE 2 END, id")
    fun observeAll(): Flow<List<MenuItemEntity>>

    @Query("SELECT * FROM menu_items WHERE id = :id")
    fun observeById(id: Int): Flow<MenuItemEntity?>

    @Query("SELECT * FROM menu_items WHERE id = :id")
    suspend fun getById(id: Int): MenuItemEntity?

    @Query("SELECT * FROM menu_items")
    suspend fun getAll(): List<MenuItemEntity>

    @Insert
    suspend fun insert(item: MenuItemEntity): Long

    @Insert
    suspend fun insertAll(items: List<MenuItemEntity>)

    @Update
    suspend fun update(item: MenuItemEntity)

    @Query("DELETE FROM menu_items WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("UPDATE menu_items SET isFavorite = NOT isFavorite WHERE id = :id")
    suspend fun toggleFavorite(id: Int)

    @Query("SELECT * FROM recipe_items")
    fun observeRecipes(): Flow<List<RecipeItemEntity>>

    @Query("SELECT * FROM recipe_items")
    suspend fun getRecipes(): List<RecipeItemEntity>

    @Query("SELECT * FROM recipe_items WHERE menuItemId = :menuItemId")
    suspend fun getRecipe(menuItemId: Int): List<RecipeItemEntity>

    @Insert
    suspend fun insertRecipe(items: List<RecipeItemEntity>)

    @Query("DELETE FROM recipe_items WHERE menuItemId = :menuItemId")
    suspend fun deleteRecipe(menuItemId: Int)

    /** Creates or updates a dish together with its recipe in one transaction. Returns the dish id. */
    @Transaction
    suspend fun saveWithRecipe(item: MenuItemEntity, recipe: List<RecipeItemEntity>): Int {
        val id = if (item.id == 0) {
            insert(item).toInt()
        } else {
            val isFavorite = getById(item.id)?.isFavorite ?: false
            update(item.copy(isFavorite = isFavorite))
            item.id
        }
        deleteRecipe(id)
        insertRecipe(recipe.map { it.copy(menuItemId = id) })
        return id
    }
}

@Dao
interface InventoryDao {
    @Query("SELECT * FROM ingredients ORDER BY name")
    fun observeIngredients(): Flow<List<IngredientEntity>>

    @Query("SELECT * FROM ingredients WHERE id = :id")
    fun observeIngredient(id: Int): Flow<IngredientEntity?>

    @Query("SELECT * FROM ingredients")
    suspend fun getIngredients(): List<IngredientEntity>

    @Query("SELECT * FROM ingredients WHERE id = :id")
    suspend fun getIngredient(id: Int): IngredientEntity?

    @Insert
    suspend fun insertIngredient(ingredient: IngredientEntity): Long

    @Insert
    suspend fun insertIngredients(ingredients: List<IngredientEntity>)

    @Query("UPDATE ingredients SET name = :name, unit = :unit, lowThreshold = :lowThreshold WHERE id = :id")
    suspend fun updateIngredientInfo(id: Int, name: String, unit: StockUnit, lowThreshold: Double)

    @Query("DELETE FROM ingredients WHERE id = :id")
    suspend fun deleteIngredient(id: Int)

    @Query("UPDATE ingredients SET stock = stock + :delta WHERE id = :id")
    suspend fun changeStock(id: Int, delta: Double)

    @Query("SELECT * FROM stock_transactions WHERE ingredientId = :ingredientId ORDER BY createdAt DESC, id DESC")
    fun observeTransactions(ingredientId: Int): Flow<List<StockTransactionEntity>>

    @Query("SELECT * FROM stock_transactions ORDER BY createdAt DESC, id DESC")
    fun observeAllTransactions(): Flow<List<StockTransactionEntity>>

    @Query("SELECT * FROM stock_transactions WHERE orderId = :orderId AND type = :type")
    suspend fun getOrderTransactions(orderId: Int, type: TransactionType): List<StockTransactionEntity>

    @Insert
    suspend fun insertTransaction(transaction: StockTransactionEntity)

    @Query(
        """
        SELECT m.id AS menuItemId, m.name AS name, m.type AS type, r.amount AS amount
        FROM recipe_items r JOIN menu_items m ON m.id = r.menuItemId
        WHERE r.ingredientId = :ingredientId
        ORDER BY m.name
        """
    )
    fun observeUsages(ingredientId: Int): Flow<List<DishUsageRow>>

    /** Writes the transaction and changes the stock by its delta atomically. */
    @Transaction
    suspend fun apply(transaction: StockTransactionEntity) {
        insertTransaction(transaction)
        changeStock(transaction.ingredientId, transaction.delta)
    }

    @Transaction
    suspend fun createWithInitialStock(ingredient: IngredientEntity): Int {
        val id = insertIngredient(ingredient.copy(stock = 0.0)).toInt()
        if (ingredient.stock > 0) {
            apply(
                StockTransactionEntity(
                    ingredientId = id,
                    type = TransactionType.RESTOCK,
                    delta = ingredient.stock,
                    note = "Начальный остаток"
                )
            )
        }
        return id
    }
}

@Dao
interface CartDao {
    @Query("SELECT * FROM cart_lines ORDER BY addedAt, `key`")
    fun observeLines(): Flow<List<CartLineEntity>>

    @Query("SELECT * FROM cart_lines ORDER BY addedAt, `key`")
    suspend fun getLines(): List<CartLineEntity>

    @Query("SELECT * FROM cart_lines WHERE `key` = :key")
    suspend fun getLine(key: String): CartLineEntity?

    @Query("SELECT * FROM cart_lines WHERE menuItemId = :menuItemId ORDER BY addedAt DESC LIMIT 1")
    suspend fun getLastLineOf(menuItemId: Int): CartLineEntity?

    @Insert
    suspend fun insert(line: CartLineEntity)

    @Query("UPDATE cart_lines SET quantity = :quantity WHERE `key` = :key")
    suspend fun setQuantity(key: String, quantity: Int)

    @Query("DELETE FROM cart_lines WHERE `key` = :key")
    suspend fun delete(key: String)

    @Query("DELETE FROM cart_lines")
    suspend fun clear()

    @Transaction
    suspend fun addOrIncrease(line: CartLineEntity) {
        val existing = getLine(line.key)
        if (existing == null) insert(line) else setQuantity(line.key, existing.quantity + line.quantity)
    }

    @Transaction
    suspend fun changeQuantity(key: String, delta: Int) {
        val existing = getLine(key) ?: return
        val quantity = existing.quantity + delta
        if (quantity > 0) setQuantity(key, quantity) else delete(key)
    }
}

@Dao
interface OrderDao {
    @Transaction
    @Query("SELECT * FROM orders ORDER BY createdAt DESC, id DESC")
    fun observeOrders(): Flow<List<OrderWithLines>>

    @Transaction
    @Query("SELECT * FROM orders WHERE id = :id")
    fun observeOrder(id: Int): Flow<OrderWithLines?>

    @Transaction
    @Query("SELECT * FROM orders WHERE id = :id")
    suspend fun getOrder(id: Int): OrderWithLines?

    @Transaction
    @Query("SELECT * FROM orders WHERE statusType NOT IN ('DELIVERED', 'CANCELLED')")
    suspend fun getActiveOrders(): List<OrderWithLines>

    @Insert
    suspend fun insertOrder(order: OrderEntity): Long

    @Insert
    suspend fun insertLines(lines: List<OrderLineEntity>)

    /** A cancelled order is final, so a late update from the processing coroutine cannot revive it. */
    @Query(
        """
        UPDATE orders SET statusType = :type, statusValue = :value, cancelReason = :reason
        WHERE id = :id AND statusType != 'CANCELLED'
        """
    )
    suspend fun updateStatus(id: Int, type: StatusType, value: Int, reason: String?)
}
