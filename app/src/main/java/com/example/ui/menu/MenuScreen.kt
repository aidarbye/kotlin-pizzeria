package com.example.ui.menu

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.R
import com.example.data.SampleData
import com.example.model.Category
import com.example.model.MenuItem
import com.example.ui.AppViewModelProvider
import com.example.ui.common.LocalSnackbarHostState
import com.example.ui.components.CategoryFilterRow
import com.example.ui.components.EmptyState
import com.example.ui.components.LoadingState
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
    onAddItem: () -> Unit,
    viewModel: MenuViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val filters by viewModel.filters.collectAsStateWithLifecycle()
    val snackbarHostState = LocalSnackbarHostState.current
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
            if (!viewModel.quickAdd(item)) return@MenuContent
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
        onOpenCart = onOpenCart,
        onAddItem = onAddItem
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
    onOpenCart: () -> Unit,
    onAddItem: () -> Unit
) {
    Scaffold(
        topBar = { PizzeriaTopBar(title = "Пиццерия ${state.pizzeriaName}") },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddItem,
                icon = { Icon(painterResource(R.drawable.ic_add), contentDescription = "Добавить блюдо") },
                text = { Text("Блюдо") }
            )
        },
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
        if (state.isLoading) {
            LoadingState(Modifier.padding(padding))
            return@Scaffold
        }
        if (state.isMenuEmpty) {
            EmptyState(
                icon = R.drawable.ic_pizza,
                title = "Меню пустое",
                subtitle = "Все блюда удалены. Добавьте первое блюдо кнопкой ниже",
                actionText = "Добавить блюдо",
                onAction = onAddItem,
                modifier = Modifier.padding(padding)
            )
            return@Scaffold
        }
        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 88.dp),
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
                    portionsLeft = state.portionsLeft[item.id],
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
            state = MenuUiState(
                pizzeriaName = "Roma",
                items = SampleData.menu,
                quantities = mapOf(2 to 1),
                portionsLeft = mapOf(7 to 4, 13 to 0),
                cartCount = 1,
                cartTotal = 10.0,
                isLoading = false
            ),
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
            onOpenCart = {},
            onAddItem = {}
        )
    }
}
