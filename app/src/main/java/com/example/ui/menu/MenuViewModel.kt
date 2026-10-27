package com.example.ui.menu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.CartRepository
import com.example.data.MenuRepository
import com.example.model.Category
import com.example.model.MenuItem
import com.example.model.OrderLine
import com.example.model.Pizza
import com.example.model.PizzaSize
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

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
    val cartCount: Int = 0,
    val cartTotal: Double = 0.0
)

class MenuViewModel(
    private val menuRepository: MenuRepository,
    private val cartRepository: CartRepository
) : ViewModel() {

    private val pizzeria = menuRepository.pizzeria

    private val _filters = MutableStateFlow(MenuFilters())
    val filters: StateFlow<MenuFilters> = _filters.asStateFlow()

    val uiState: StateFlow<MenuUiState> =
        combine(_filters, menuRepository.favorites, cartRepository.cart) { filters, favorites, cart ->
            MenuUiState(
                pizzeriaName = pizzeria.name,
                items = pizzeria.search(
                    query = filters.query,
                    category = filters.category,
                    vegetarianOnly = filters.vegetarianOnly,
                    favoriteIds = favorites.takeIf { filters.favoritesOnly }
                ),
                favorites = favorites,
                quantities = cart.lines.groupBy { it.item.id }.mapValues { (_, lines) -> lines.sumOf { it.quantity } },
                cartCount = cart.count,
                cartTotal = cart.lines.sumOf { it.subtotal }
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = MenuUiState(pizzeriaName = pizzeria.name, items = pizzeria.menu)
        )

    fun onQueryChange(query: String) = _filters.update { it.copy(query = query) }

    fun onCategorySelect(category: Category) = _filters.update { it.copy(category = category) }

    fun onVegetarianOnlyChange(enabled: Boolean) = _filters.update { it.copy(vegetarianOnly = enabled) }

    fun onFavoritesOnlyChange(enabled: Boolean) = _filters.update { it.copy(favoritesOnly = enabled) }

    fun toggleFavorite(item: MenuItem) = menuRepository.toggleFavorite(item.id)

    fun quickAdd(item: MenuItem) =
        cartRepository.add(OrderLine(item, quantity = 1, size = if (item is Pizza) PizzaSize.MEDIUM else null))

    fun removeOne(item: MenuItem) = cartRepository.removeOneOf(item.id)
}
