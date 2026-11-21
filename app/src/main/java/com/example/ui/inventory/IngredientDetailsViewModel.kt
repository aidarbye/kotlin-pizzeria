package com.example.ui.inventory

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.data.InventoryRepository
import com.example.model.DishUsage
import com.example.model.Ingredient
import com.example.model.StockTransaction
import com.example.model.TransactionType
import com.example.ui.common.UserMessages
import com.example.ui.editor.toDecimal
import com.example.ui.navigation.IngredientDetailsRoute
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.abs

/** Totals of all transactions of one ingredient, by type. */
data class StockTotals(
    val received: Double = 0.0,
    val usedInOrders: Double = 0.0,
    val returned: Double = 0.0,
    val writtenOff: Double = 0.0
)

data class IngredientDetailsUiState(
    val ingredient: Ingredient? = null,
    val usages: List<DishUsage> = emptyList(),
    val transactions: List<StockTransaction> = emptyList(),
    val totals: StockTotals = StockTotals(),
    val isLoading: Boolean = true
)

class IngredientDetailsViewModel(
    savedStateHandle: SavedStateHandle,
    private val inventoryRepository: InventoryRepository,
    private val messages: UserMessages
) : ViewModel() {

    val ingredientId: Int = savedStateHandle.toRoute<IngredientDetailsRoute>().ingredientId

    val uiState: StateFlow<IngredientDetailsUiState> =
        combine(
            inventoryRepository.ingredient(ingredientId),
            inventoryRepository.usages(ingredientId),
            inventoryRepository.transactions(ingredientId)
        ) { ingredient, usages, transactions ->
            val byType = transactions.groupBy { it.type }.mapValues { (_, list) -> abs(list.sumOf { it.delta }) }
            IngredientDetailsUiState(
                ingredient = ingredient,
                usages = usages,
                transactions = transactions,
                totals = StockTotals(
                    received = byType[TransactionType.RESTOCK] ?: 0.0,
                    usedInOrders = byType[TransactionType.ORDER] ?: 0.0,
                    returned = byType[TransactionType.RETURN] ?: 0.0,
                    writtenOff = byType[TransactionType.WRITE_OFF] ?: 0.0
                ),
                isLoading = false
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), IngredientDetailsUiState())

    /** Validates again (the dialog already did) and applies the operation. Returns false if the input is invalid. */
    fun applyOperation(operation: StockOperation, amountText: String, note: String): Boolean {
        val ingredient = uiState.value.ingredient ?: return false
        if (!validateStockAmount(amountText, operation, ingredient, note).isValid) return false
        val amount = amountText.toDecimal() ?: return false
        viewModelScope.launch {
            when (operation) {
                StockOperation.RESTOCK -> {
                    inventoryRepository.restock(ingredientId, amount, note.trim().ifEmpty { "Поставка" })
                    messages.show("${ingredient.name}: +${ingredient.format(amount)}")
                }
                StockOperation.WRITE_OFF -> {
                    val done = inventoryRepository.writeOff(ingredientId, amount, note.trim())
                    messages.show(
                        if (done) "${ingredient.name}: списано ${ingredient.format(amount)}"
                        else "Нельзя списать больше, чем есть на складе"
                    )
                }
            }
        }
        return true
    }

    fun delete(onDeleted: () -> Unit) {
        val name = uiState.value.ingredient?.name ?: return
        viewModelScope.launch {
            inventoryRepository.delete(ingredientId)
            messages.show("Ингредиент «$name» удалён")
            onDeleted()
        }
    }
}
