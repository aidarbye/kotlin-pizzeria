package com.example.ui.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.data.CartRepository
import com.example.data.MenuRepository
import com.example.model.Extra
import com.example.model.MenuItem
import com.example.model.OrderLine
import com.example.model.Pizza
import com.example.model.PizzaSize
import com.example.model.priceFor
import com.example.ui.navigation.ItemDetailsRoute
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class ItemDetailsUiState(
    val item: MenuItem? = null,
    val size: PizzaSize = PizzaSize.MEDIUM,
    val availableExtras: List<Extra> = emptyList(),
    val selectedExtras: Set<Extra> = emptySet(),
    val quantity: Int = 1,
    val isFavorite: Boolean = false,
    val inCart: Int = 0
) {
    val isPizza: Boolean
        get() = item is Pizza

    val total: Double
        get() = (item?.priceFor(size.takeIf { isPizza }, selectedExtras) ?: 0.0) * quantity
}

class ItemDetailsViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val menuRepository: MenuRepository,
    private val cartRepository: CartRepository
) : ViewModel() {

    private val item: MenuItem? = menuRepository.findItem(savedStateHandle.toRoute<ItemDetailsRoute>().itemId)
    private val extras: List<Extra> = if (item is Pizza) menuRepository.extras else emptyList()

    private val size = savedStateHandle.getStateFlow(KEY_SIZE, PizzaSize.MEDIUM)
    private val extraIds = savedStateHandle.getStateFlow(KEY_EXTRAS, arrayListOf<Int>())
    private val quantity = savedStateHandle.getStateFlow(KEY_QUANTITY, 1)

    val uiState: StateFlow<ItemDetailsUiState> =
        combine(size, extraIds, quantity, menuRepository.favorites, cartRepository.cart) { size, ids, quantity, favorites, cart ->
            buildState(size, ids, quantity, favorites, cart.quantityOf(item?.id ?: -1))
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = buildState(size.value, extraIds.value, quantity.value, menuRepository.favorites.value, 0)
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
        item?.let { menuRepository.toggleFavorite(it.id) }
    }

    fun addToCart() {
        val state = uiState.value
        val item = state.item ?: return
        cartRepository.add(
            OrderLine(
                item = item,
                quantity = state.quantity,
                size = state.size.takeIf { state.isPizza },
                extras = state.selectedExtras
            )
        )
    }

    private fun buildState(
        size: PizzaSize,
        extraIds: List<Int>,
        quantity: Int,
        favorites: Set<Int>,
        inCart: Int
    ) = ItemDetailsUiState(
        item = item,
        size = size,
        availableExtras = extras,
        selectedExtras = extras.filter { it.id in extraIds }.toSet(),
        quantity = quantity,
        isFavorite = item != null && item.id in favorites,
        inCart = inCart
    )

    private companion object {
        const val KEY_SIZE = "size"
        const val KEY_EXTRAS = "extras"
        const val KEY_QUANTITY = "quantity"
    }
}
