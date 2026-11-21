package com.example.ui.inventory

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.R
import com.example.data.SampleData
import com.example.ui.AppViewModelProvider
import com.example.ui.components.EmptyState
import com.example.ui.components.IngredientCard
import com.example.ui.components.LoadingState
import com.example.ui.components.PizzeriaTopBar
import com.example.ui.components.SectionTitle
import com.example.ui.components.StatTile
import com.example.ui.components.formatDateTime
import com.example.ui.components.transactionColor
import com.example.ui.theme.PizzeriaTheme

@Composable
fun InventoryScreen(
    onOpenIngredient: (Int) -> Unit,
    onAddIngredient: () -> Unit,
    viewModel: InventoryViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    InventoryContent(state = state, onOpenIngredient = onOpenIngredient, onAddIngredient = onAddIngredient)
}

@Composable
fun InventoryContent(
    state: InventoryUiState,
    onOpenIngredient: (Int) -> Unit,
    onAddIngredient: () -> Unit
) {
    Scaffold(
        topBar = { PizzeriaTopBar(title = "Склад") },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddIngredient,
                icon = { Icon(painterResource(R.drawable.ic_add), contentDescription = null) },
                text = { Text("Ингредиент") }
            )
        }
    ) { padding ->
        if (state.isLoading) {
            LoadingState(Modifier.padding(padding))
            return@Scaffold
        }
        if (state.rows.isEmpty()) {
            EmptyState(
                icon = R.drawable.ic_inventory,
                title = "Склад пуст",
                subtitle = "Добавьте ингредиенты, чтобы учитывать их расход в заказах",
                actionText = "Добавить ингредиент",
                onAction = onAddIngredient,
                modifier = Modifier.padding(padding)
            )
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item(key = "stats") { StatsRow(state.stats) }
            if (state.recent.isNotEmpty()) {
                item(key = "recent") { RecentActivityCard(state.recent) }
            }
            item(key = "title") { SectionTitle("Ингредиенты") }
            items(state.rows, key = { it.ingredient.id }) { row ->
                IngredientCard(
                    ingredient = row.ingredient,
                    usedIn = row.usedIn,
                    onClick = { onOpenIngredient(row.ingredient.id) },
                    modifier = Modifier.animateItem()
                )
            }
        }
    }
}

@Composable
private fun StatsRow(stats: InventoryStats) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        StatTile(stats.total.toString(), "позиций", Modifier.weight(1f))
        StatTile(
            stats.low.toString(),
            "заканчив.",
            Modifier.weight(1f),
            color = if (stats.low > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
        )
        StatTile(
            stats.out.toString(),
            "нет",
            Modifier.weight(1f),
            color = if (stats.out > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
        )
        StatTile(stats.ordersServed.toString(), "заказов", Modifier.weight(1f))
    }
}

@Composable
private fun RecentActivityCard(rows: List<ActivityRow>) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Последние операции", style = MaterialTheme.typography.titleMedium)
            rows.forEach { row ->
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        painterResource(R.drawable.ic_history),
                        contentDescription = null,
                        tint = transactionColor(row.type)
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(row.title, style = MaterialTheme.typography.titleSmall)
                        Text(
                            row.details,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2
                        )
                    }
                    Text(
                        row.createdAt.formatDateTime(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun InventoryPreview() {
    PizzeriaTheme {
        InventoryContent(
            state = InventoryUiState(
                rows = SampleData.ingredients.take(5).map { IngredientRow(it, 3) },
                stats = InventoryStats(total = 26, low = 2, out = 1, ordersServed = 4),
                isLoading = false
            ),
            onOpenIngredient = {},
            onAddIngredient = {}
        )
    }
}
