package com.example.ui.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.InventoryRepository
import com.example.data.MenuRepository
import com.example.model.Ingredient
import com.example.model.StockTransaction
import com.example.model.TransactionType
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class IngredientRow(
    val ingredient: Ingredient,
    val usedIn: Int
)

/**
 * One entry of the activity feed. All transactions of one order are shown as a single entry,
 * other operations (restock, write-off) are shown one by one.
 */
data class ActivityRow(
    val key: String,
    val type: TransactionType,
    val title: String,
    val details: String,
    val createdAt: Long
)

data class InventoryStats(
    val total: Int = 0,
    val low: Int = 0,
    val out: Int = 0,
    val ordersServed: Int = 0,
    val writeOffs: Int = 0
)

data class InventoryUiState(
    /** Ingredients that need attention (out of stock, then low) come first. */
    val rows: List<IngredientRow> = emptyList(),
    val stats: InventoryStats = InventoryStats(),
    val recent: List<ActivityRow> = emptyList(),
    val isLoading: Boolean = true
)

class InventoryViewModel(
    inventoryRepository: InventoryRepository,
    menuRepository: MenuRepository
) : ViewModel() {

    val uiState: StateFlow<InventoryUiState> =
        combine(inventoryRepository.ingredients, menuRepository.recipes, inventoryRepository.allTransactions) { ingredients, recipes, transactions ->
            val usage = recipes.values.flatten().groupingBy { it.ingredientId }.eachCount()
            val byId = ingredients.associateBy { it.id }
            InventoryUiState(
                rows = ingredients
                    .sortedWith(compareBy<Ingredient>({ !it.isOut }, { !it.isLow }, { it.name }))
                    .map { IngredientRow(it, usage[it.id] ?: 0) },
                stats = InventoryStats(
                    total = ingredients.size,
                    low = ingredients.count { it.isLow },
                    out = ingredients.count { it.isOut },
                    ordersServed = transactions.filter { it.type == TransactionType.ORDER }.mapNotNull { it.orderId }.distinct().size,
                    writeOffs = transactions.count { it.type == TransactionType.WRITE_OFF }
                ),
                recent = activity(transactions, byId).take(RECENT_COUNT),
                isLoading = false
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), InventoryUiState())

    private fun activity(transactions: List<StockTransaction>, ingredients: Map<Int, Ingredient>): List<ActivityRow> {
        val groups = linkedMapOf<String, MutableList<StockTransaction>>()
        transactions.forEach { tx ->
            val key = if (tx.orderId != null) "${tx.type}-${tx.orderId}" else "tx-${tx.id}"
            groups.getOrPut(key) { mutableListOf() } += tx
        }
        return groups.mapNotNull { (key, group) ->
            val first = group.first()
            val changes = group.mapNotNull { tx ->
                ingredients[tx.ingredientId]?.let { "${it.name} ${tx.delta.signed()}${it.format(kotlin.math.abs(tx.delta))}" }
            }
            if (changes.isEmpty()) return@mapNotNull null
            val title = when (first.type) {
                TransactionType.ORDER -> "Заказ #${first.orderId}"
                TransactionType.RETURN -> "Возврат по заказу #${first.orderId}"
                else -> "${first.type.title}: ${ingredients[first.ingredientId]?.name.orEmpty()}"
            }
            val details = if (first.orderId == null) {
                listOf(changes.first(), first.note).filter { it.isNotBlank() }.joinToString(" · ")
            } else {
                changes.joinToString()
            }
            ActivityRow(key, first.type, title, details, first.createdAt)
        }
    }

    private fun Double.signed(): String = if (this >= 0) "+" else "−"

    private companion object {
        const val RECENT_COUNT = 5
    }
}
