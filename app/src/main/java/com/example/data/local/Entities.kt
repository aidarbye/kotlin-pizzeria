package com.example.data.local

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.example.model.DeliveryType
import com.example.model.DishType
import com.example.model.PizzaSize
import com.example.model.StockUnit
import com.example.model.TransactionType

@Entity(tableName = "menu_items")
data class MenuItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: DishType,
    val name: String,
    val price: Double,
    val description: String,
    /** Pizza toppings separated by commas. */
    val toppings: String = "",
    val isVegetarian: Boolean = false,
    val isSpicy: Boolean = false,
    val volumeMl: Int = 0,
    val isSugarFree: Boolean = false,
    val isFavorite: Boolean = false
)

@Entity(tableName = "ingredients")
data class IngredientEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val unit: StockUnit,
    val stock: Double,
    val lowThreshold: Double
)

/** Many-to-many link between a dish and the ingredients its recipe consumes. */
@Entity(
    tableName = "recipe_items",
    primaryKeys = ["menuItemId", "ingredientId"],
    foreignKeys = [
        ForeignKey(MenuItemEntity::class, ["id"], ["menuItemId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(IngredientEntity::class, ["id"], ["ingredientId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("ingredientId")]
)
data class RecipeItemEntity(
    val menuItemId: Int,
    val ingredientId: Int,
    val amount: Double
)

/** Every change of the stock is a transaction, so the history of an ingredient can always be explained. */
@Entity(
    tableName = "stock_transactions",
    foreignKeys = [ForeignKey(IngredientEntity::class, ["id"], ["ingredientId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("ingredientId"), Index("orderId")]
)
data class StockTransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val ingredientId: Int,
    val type: TransactionType,
    val delta: Double,
    val orderId: Int? = null,
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "cart_lines",
    foreignKeys = [ForeignKey(MenuItemEntity::class, ["id"], ["menuItemId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("menuItemId")]
)
data class CartLineEntity(
    @PrimaryKey val key: String,
    val menuItemId: Int,
    val quantity: Int,
    val size: PizzaSize?,
    /** Ids of the selected extras separated by commas. */
    val extraIds: String,
    val addedAt: Long = System.currentTimeMillis()
)

enum class StatusType { RECEIVED, COOKING, DELIVERING, DELIVERED, CANCELLED }

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val customerName: String,
    val phone: String,
    val address: String,
    val deliveryType: DeliveryType,
    val subtotal: Double,
    val discount: Double,
    val deliveryFee: Double,
    val promoCode: String?,
    val comment: String,
    val createdAt: Long,
    val statusType: StatusType,
    /** Cooking progress or delivery minutes, depending on the status. */
    val statusValue: Int = 0,
    val cancelReason: String? = null
)

/**
 * A line of a placed order. It keeps a snapshot of the dish (name, type, price), so the order history
 * stays correct after the dish is edited or removed from the menu.
 */
@Entity(
    tableName = "order_lines",
    foreignKeys = [ForeignKey(OrderEntity::class, ["id"], ["orderId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("orderId")]
)
data class OrderLineEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val orderId: Int,
    val menuItemId: Int,
    val itemType: DishType,
    val itemName: String,
    val basePrice: Double,
    val quantity: Int,
    val size: PizzaSize?,
    val extraIds: String
)

data class OrderWithLines(
    @Embedded val order: OrderEntity,
    @Relation(parentColumn = "id", entityColumn = "orderId")
    val lines: List<OrderLineEntity>
)

/** Result of a JOIN between recipes and menu items. */
data class DishUsageRow(
    val menuItemId: Int,
    val name: String,
    val type: DishType,
    val amount: Double
)
