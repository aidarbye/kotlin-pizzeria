package com.example.ui.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.data.CartRepository
import com.example.data.InventoryRepository
import com.example.data.MenuRepository
import com.example.model.Extra
import com.example.model.Ingredient
import com.example.model.MenuItem
import com.example.model.OrderLine
import com.example.model.Pizza
import com.example.model.PizzaSize
import com.example.model.portionFactor
import com.example.model.priceFor
import com.example.service.StockCalculator
import com.example.ui.common.UserMessages
import com.example.ui.navigation.ItemDetailsRoute
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** One ingredient of the dish with the amount for the selected size. */
data class RecipeRow(
    val ingredient: Ingredient,
    val amount: Double
)

data class ItemDetailsUiState(
    val item: MenuItem? = null,
    val size: PizzaSize = PizzaSize.MEDIUM,
    val availableExtras: List<Extra> = emptyList(),
    val selectedExtras: Set<Extra> = emptySet(),
    val quantity: Int = 1,
    val isFavorite: Boolean = false,
    val inCart: Int = 0,
    val recipe: List<RecipeRow> = emptyList(),
    /** How many more portions of the selected size the stock allows, null when it is not tracked. */
    val portionsLeft: Int? = null,
    val isLoading: Boolean = true
) {
    val isPizza: Boolean
        get() = item is Pizza

    val total: Double
        get() = (item?.priceFor(size.takeIf { isPizza }, selectedExtras) ?: 0.0) * quantity

    val canAdd: Boolean
        get() = portionsLeft == null || quantity <= portionsLeft
}

private data class Selection(val size: PizzaSize, val extraIds: List<Int>, val quantity: Int)

class ItemDetailsViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val menuRepository: MenuRepository,
    inventoryRepository: InventoryRepository,
    private val cartRepository: CartRepository,
    private val messages: UserMessages
) : ViewModel() {

    val itemId: Int = savedStateHandle.toRoute<ItemDetailsRoute>().itemId

    private val size = savedStateHandle.getStateFlow(KEY_SIZE, PizzaSize.MEDIUM)
    private val extraIds = savedStateHandle.getStateFlow(KEY_EXTRAS, arrayListOf<Int>())
    private val quantity = savedStateHandle.getStateFlow(KEY_QUANTITY, 1)

    private val selection = combine(size, extraIds, quantity, ::Selection)
    private val stock = combine(inventoryRepository.ingredients, menuRepository.recipes, ::Pair)

    val uiState: StateFlow<ItemDetailsUiState> =
        combine(menuRepository.item(itemId), selection, menuRepository.favorites, cartRepository.cart, stock) { item, selection, favorites, cart, (ingredients, recipes) ->
            val isPizza = item is Pizza
            val extras = if (isPizza) menuRepository.extras else emptyList()
            val recipe = recipes[itemId].orEmpty()
            val ingredientsById = ingredients.associateBy { it.id }
            val sizeOrNull = selection.size.takeIf { isPizza }
            val remaining = StockCalculator.remaining(ingredients, StockCalculator.requirements(cart.lines, recipes))
            ItemDetailsUiState(
                item = item,
                size = selection.size,
                availableExtras = extras,
                selectedExtras = extras.filter { it.id in selection.extraIds }.toSet(),
                quantity = selection.quantity,
                isFavorite = itemId in favorites,
                inCart = cart.quantityOf(itemId),
                recipe = recipe.mapNotNull { entry ->
                    ingredientsById[entry.ingredientId]?.let { RecipeRow(it, entry.amount * sizeOrNull.portionFactor()) }
                },
                portionsLeft = StockCalculator.portionsLeft(recipe, sizeOrNull, remaining),
                isLoading = false
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ItemDetailsUiState()
        )

    fun selectSize(size: PizzaSize) {
        savedStateHandle[KEY_SIZE] = size
    }

    fun toggleExtra(extra: Extra) {
        val ids = extraIds.value
        savedStateHandle[KEY_EXTRAS] = ArrayList(if (extra.id in ids) ids - extra.id else ids + extra.id)
    }

    fun changeQuantity(delta: Int) {
        savedStateHandle[KEY_QUANTITY] = (quantity.value + delta).coerceIn(1, 20)
    }

    fun toggleFavorite() {
        viewModelScope.launch { menuRepository.toggleFavorite(itemId) }
    }

    fun addToCart(onAdded: () -> Unit) {
        val state = uiState.value
        val item = state.item ?: return
        if (!state.canAdd) {
            messages.show("На складе хватит только на ${state.portionsLeft} шт.")
            return
        }
        viewModelScope.launch {
            cartRepository.add(
                OrderLine(
                    item = item,
                    quantity = state.quantity,
                    size = state.size.takeIf { state.isPizza },
                    extras = state.selectedExtras
                )
            )
            onAdded()
        }
    }

    fun delete(onDeleted: () -> Unit) {
        val name = uiState.value.item?.name ?: return
        viewModelScope.launch {
            menuRepository.delete(itemId)
            messages.show("«$name» удалено из меню")
            onDeleted()
        }
    }

    private companion object {
        const val KEY_SIZE = "size"
        const val KEY_EXTRAS = "extras"
        const val KEY_QUANTITY = "quantity"
    }
}
