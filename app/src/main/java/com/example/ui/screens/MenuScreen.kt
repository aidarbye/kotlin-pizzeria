package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.data.SampleData
import com.example.model.Category
import com.example.model.MenuItem
import com.example.ui.PizzeriaUiState
import com.example.ui.components.CategoryFilterRow
import com.example.ui.components.LabeledCheckbox
import com.example.ui.components.MenuItemCard
import com.example.ui.components.SearchField
import com.example.ui.theme.PizzeriaTheme

@Composable
fun MenuScreen(
    state: PizzeriaUiState,
    onQueryChange: (String) -> Unit,
    onCategorySelect: (Category) -> Unit,
    onVegetarianOnlyChange: (Boolean) -> Unit,
    onAdd: (MenuItem) -> Unit,
    onRemove: (MenuItem) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { SearchField(query = state.query, onQueryChange = onQueryChange) }
        item { CategoryFilterRow(selected = state.category, onSelect = onCategorySelect) }
        item {
            LabeledCheckbox(
                text = "Только вегетарианское",
                checked = state.vegetarianOnly,
                onCheckedChange = onVegetarianOnlyChange
            )
        }
        if (state.visibleMenu.isEmpty()) {
            item {
                Text(
                    text = "Ничего не найдено",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        items(state.visibleMenu, key = { it.id }) { item ->
            MenuItemCard(
                item = item,
                quantity = state.cart[item.id] ?: 0,
                onAdd = { onAdd(item) },
                onRemove = { onRemove(item) },
                modifier = Modifier.animateItem()
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MenuScreenPreview() {
    PizzeriaTheme {
        MenuScreen(
            state = PizzeriaUiState(visibleMenu = SampleData.menu, cart = mapOf(2 to 1)),
            onQueryChange = {},
            onCategorySelect = {},
            onVegetarianOnlyChange = {},
            onAdd = {},
            onRemove = {}
        )
    }
}
