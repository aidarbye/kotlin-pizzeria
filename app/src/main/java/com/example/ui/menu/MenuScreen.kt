package com.example.ui.menu

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.SampleData
import com.example.model.Category
import com.example.model.MenuItem
import com.example.ui.AppViewModelProvider
import com.example.ui.components.CategoryFilterRow
import com.example.ui.components.MenuItemCard
import com.example.ui.components.PizzeriaTopBar
import com.example.ui.components.SearchField
import com.example.ui.components.SummaryBar
import com.example.ui.components.ToggleChip
import com.example.ui.theme.PizzeriaTheme
import kotlinx.coroutines.launch

@Composable
fun MenuScreen(
    onOpenItem: (Int) -> Unit,
    onOpenCart: () -> Unit,
    viewModel: MenuViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val filters by viewModel.filters.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    MenuContent(
        state = state,
        filters = filters,
        snackbarHostState = snackbarHostState,
        onQueryChange = viewModel::onQueryChange,
        onCategorySelect = viewModel::onCategorySelect,
        onVegetarianOnlyChange = viewModel::onVegetarianOnlyChange,
        onFavoritesOnlyChange = viewModel::onFavoritesOnlyChange,
        onOpenItem = { onOpenItem(it.id) },
        onAdd = { item ->
            viewModel.quickAdd(item)
            scope.launch {
                snackbarHostState.currentSnackbarData?.dismiss()
                val result = snackbarHostState.showSnackbar(
                    message = "${item.name} в корзине",
                    actionLabel = "Корзина",
                    duration = SnackbarDuration.Short
                )
                if (result == SnackbarResult.ActionPerformed) onOpenCart()
            }
        },
        onRemove = viewModel::removeOne,
        onToggleFavorite = viewModel::toggleFavorite,
        onOpenCart = onOpenCart
    )
}

@Composable
fun MenuContent(
    state: MenuUiState,
    filters: MenuFilters,
    snackbarHostState: SnackbarHostState,
    onQueryChange: (String) -> Unit,
    onCategorySelect: (Category) -> Unit,
    onVegetarianOnlyChange: (Boolean) -> Unit,
    onFavoritesOnlyChange: (Boolean) -> Unit,
    onOpenItem: (MenuItem) -> Unit,
    onAdd: (MenuItem) -> Unit,
    onRemove: (MenuItem) -> Unit,
    onToggleFavorite: (MenuItem) -> Unit,
    onOpenCart: () -> Unit
) {
    Scaffold(
        topBar = { PizzeriaTopBar(title = "Пиццерия ${state.pizzeriaName}") },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (state.cartCount > 0) {
                SummaryBar(
                    label = "В корзине: ${state.cartCount} шт.",
                    total = state.cartTotal,
                    actionText = "Корзина",
                    onAction = onOpenCart
                )
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { SearchField(query = filters.query, onQueryChange = onQueryChange) }
            item { CategoryFilterRow(selected = filters.category, onSelect = onCategorySelect) }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ToggleChip("Вегетарианское", filters.vegetarianOnly, onVegetarianOnlyChange)
                    ToggleChip("Избранное", filters.favoritesOnly, onFavoritesOnlyChange)
                }
            }
            if (state.items.isEmpty()) {
                item {
                    Text(
                        text = if (filters.favoritesOnly && state.favorites.isEmpty()) {
                            "В избранном пока пусто. Нажмите ♡ у блюда, чтобы добавить его"
                        } else {
                            "Ничего не найдено"
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            items(state.items, key = { it.id }) { item ->
                MenuItemCard(
                    item = item,
                    quantity = state.quantities[item.id] ?: 0,
                    isFavorite = item.id in state.favorites,
                    onOpen = { onOpenItem(item) },
                    onAdd = { onAdd(item) },
                    onRemove = { onRemove(item) },
                    onToggleFavorite = { onToggleFavorite(item) },
                    modifier = Modifier.animateItem()
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MenuScreenPreview() {
    PizzeriaTheme {
        MenuContent(
            state = MenuUiState(pizzeriaName = "Roma", items = SampleData.menu, quantities = mapOf(2 to 1), cartCount = 1, cartTotal = 10.0),
            filters = MenuFilters(),
            snackbarHostState = remember { SnackbarHostState() },
            onQueryChange = {},
            onCategorySelect = {},
            onVegetarianOnlyChange = {},
            onFavoritesOnlyChange = {},
            onOpenItem = {},
            onAdd = {},
            onRemove = {},
            onToggleFavorite = {},
            onOpenCart = {}
        )
    }
}
