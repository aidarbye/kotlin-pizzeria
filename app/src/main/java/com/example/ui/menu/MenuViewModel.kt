package com.example.ui.menu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.CartRepository
import com.example.data.InventoryRepository
import com.example.data.MenuRepository
import com.example.model.Category
import com.example.model.MenuItem
import com.example.model.OrderLine
import com.example.model.Pizza
import com.example.model.PizzaSize
import com.example.service.Pizzeria
import com.example.service.StockCalculator
import com.example.ui.common.UserMessages
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MenuFilters(
    val query: String = "",
    val category: Category = Category.ALL,
    val vegetarianOnly: Boolean = false,
    val favoritesOnly: Boolean = false
)

data class MenuUiState(
    val pizzeriaName: String = "",
    val items: List<MenuItem> = emptyList(),
    val favorites: Set<Int> = emptySet(),
    val quantities: Map<Int, Int> = emptyMap(),
    /** Portions that can still be added (medium size for pizzas). No entry means the stock is not tracked. */
    val portionsLeft: Map<Int, Int> = emptyMap(),
    val cartCount: Int = 0,
    val cartTotal: Double = 0.0,
    val isMenuEmpty: Boolean = false,
    val isLoading: Boolean = true
)

class MenuViewModel(
    private val menuRepository: MenuRepository,
    inventoryRepository: InventoryRepository,
    private val cartRepository: CartRepository,
    private val pizzeria: Pizzeria,
    private val messages: UserMessages
) : ViewModel() {

    private val _filters = MutableStateFlow(MenuFilters())
    val filters: StateFlow<MenuFilters> = _filters.asStateFlow()

    private val stock = combine(inventoryRepository.ingredients, menuRepository.recipes, ::Pair)

    val uiState: StateFlow<MenuUiState> =
        combine(_filters, menuRepository.menu, menuRepository.favorites, cartRepository.cart, stock) { filters, menu, favorites, cart, (ingredients, recipes) ->
            val remaining = StockCalculator.remaining(ingredients, StockCalculator.requirements(cart.lines, recipes))
            MenuUiState(
                pizzeriaName = pizzeria.name,
                items = pizzeria.search(
                    menu = menu,
                    query = filters.query,
                    category = filters.category,
                    vegetarianOnly = filters.vegetarianOnly,
                    favoriteIds = favorites.takeIf { filters.favoritesOnly }
                ),
                favorites = favorites,
                quantities = cart.lines.groupBy { it.item.id }.mapValues { (_, lines) -> lines.sumOf { it.quantity } },
                portionsLeft = menu.mapNotNull { item ->
                    StockCalculator.portionsLeft(recipes[item.id].orEmpty(), defaultSize(item), remaining)?.let { item.id to it }
                }.toMap(),
                cartCount = cart.count,
                cartTotal = cart.lines.sumOf { it.subtotal },
                isMenuEmpty = menu.isEmpty(),
                isLoading = false
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = MenuUiState(pizzeriaName = pizzeria.name)
        )

    fun onQueryChange(query: String) = _filters.update { it.copy(query = query) }

    fun onCategorySelect(category: Category) = _filters.update { it.copy(category = category) }

    fun onVegetarianOnlyChange(enabled: Boolean) = _filters.update { it.copy(vegetarianOnly = enabled) }

    fun onFavoritesOnlyChange(enabled: Boolean) = _filters.update { it.copy(favoritesOnly = enabled) }

    fun toggleFavorite(item: MenuItem) {
        viewModelScope.launch { menuRepository.toggleFavorite(item.id) }
    }

    /** Returns false when the dish cannot be added because the ingredients ran out. */
    fun quickAdd(item: MenuItem): Boolean {
        if (uiState.value.portionsLeft[item.id] == 0) {
            messages.show("«${item.name}» закончилось: не хватает ингредиентов на складе")
            return false
        }
        viewModelScope.launch { cartRepository.add(OrderLine(item, quantity = 1, size = defaultSize(item))) }
        return true
    }

    fun removeOne(item: MenuItem) {
        viewModelScope.launch { cartRepository.removeOneOf(item.id) }
    }

    private fun defaultSize(item: MenuItem): PizzaSize? = if (item is Pizza) PizzaSize.MEDIUM else null
}
