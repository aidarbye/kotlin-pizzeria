package com.example.ui.inventory

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.R
import com.example.model.DishUsage
import com.example.model.Ingredient
import com.example.ui.AppViewModelProvider
import com.example.ui.components.ConfirmDialog
import com.example.ui.components.EmptyState
import com.example.ui.components.LoadingState
import com.example.ui.components.PizzeriaTopBar
import com.example.ui.components.PriceRow
import com.example.ui.components.SectionTitle
import com.example.ui.components.StockLabel
import com.example.ui.components.StockLevelBar
import com.example.ui.components.StockOperationDialog
import com.example.ui.components.TransactionRow

@Composable
fun IngredientDetailsScreen(
    onBack: () -> Unit,
    onEdit: (Int) -> Unit,
    onOpenDish: (Int) -> Unit,
    viewModel: IngredientDetailsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    IngredientDetailsContent(
        state = state,
        onBack = onBack,
        onEdit = { onEdit(viewModel.ingredientId) },
        onDelete = { viewModel.delete(onDeleted = onBack) },
        onOperation = viewModel::applyOperation,
        onOpenDish = onOpenDish
    )
}

@Composable
fun IngredientDetailsContent(
    state: IngredientDetailsUiState,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onOperation: (StockOperation, String, String) -> Boolean,
    onOpenDish: (Int) -> Unit
) {
    val ingredient = state.ingredient
    var operation by rememberSaveable { mutableStateOf<StockOperation?>(null) }
    var confirmDelete by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            PizzeriaTopBar(title = ingredient?.name ?: "Ингредиент", onBack = onBack) {
                if (ingredient != null) {
                    IconButton(onClick = onEdit) {
                        Icon(painterResource(R.drawable.ic_edit), contentDescription = "Редактировать")
                    }
                    IconButton(onClick = { confirmDelete = true }) {
                        Icon(painterResource(R.drawable.ic_delete), contentDescription = "Удалить")
                    }
                }
            }
        }
    ) { padding ->
        if (state.isLoading) {
            LoadingState(Modifier.padding(padding))
            return@Scaffold
        }
        if (ingredient == null) {
            EmptyState(
                icon = R.drawable.ic_inventory,
                title = "Ингредиент не найден",
                subtitle = "Возможно, его удалили",
                actionText = "Назад",
                onAction = onBack,
                modifier = Modifier.padding(padding)
            )
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item(key = "stock") {
                StockCard(
                    ingredient = ingredient,
                    onRestock = { operation = StockOperation.RESTOCK },
                    onWriteOff = { operation = StockOperation.WRITE_OFF }
                )
            }
            item(key = "totals") { TotalsCard(ingredient, state.totals) }
            item(key = "usages-title") { SectionTitle("Используется в блюдах") }
            if (state.usages.isEmpty()) {
                item(key = "usages-empty") {
                    Text(
                        "Пока ни в одном рецепте. Добавьте его в рецепт при редактировании блюда",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                item(key = "usages") { UsagesCard(ingredient, state.usages, onOpenDish) }
            }
            item(key = "history-title") { SectionTitle("История операций") }
            if (state.transactions.isEmpty()) {
                item(key = "history-empty") {
                    Text(
                        "Операций ещё не было",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            items(state.transactions, key = { it.id }) { transaction ->
                TransactionRow(transaction = transaction, ingredient = ingredient, modifier = Modifier.animateItem())
            }
        }
    }

    val current = operation
    if (current != null && ingredient != null) {
        StockOperationDialog(
            operation = current,
            ingredient = ingredient,
            onConfirm = { amount, note -> if (onOperation(current, amount, note)) operation = null },
            onDismiss = { operation = null }
        )
    }

    if (confirmDelete && ingredient != null) {
        val usedIn = state.usages.size
        ConfirmDialog(
            title = "Удалить «${ingredient.name}»?",
            text = buildString {
                append("История операций с ингредиентом тоже удалится.")
                if (usedIn > 0) append(" Он будет убран из рецептов $usedIn блюд, и их расход больше не будет учитываться.")
            },
            confirmText = "Удалить",
            onConfirm = {
                confirmDelete = false
                onDelete()
            },
            onDismiss = { confirmDelete = false }
        )
    }
}

@Composable
private fun StockCard(ingredient: Ingredient, onRestock: () -> Unit, onWriteOff: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    ingredient.format(ingredient.stock),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                StockLabel(ingredient)
            }
            StockLevelBar(ingredient)
            Text(
                "Порог «заканчивается»: ${ingredient.format(ingredient.lowThreshold)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onRestock, modifier = Modifier.weight(1f)) { Text("Пополнить") }
                OutlinedButton(onClick = onWriteOff, enabled = !ingredient.isOut, modifier = Modifier.weight(1f)) {
                    Text("Списать")
                }
            }
        }
    }
}

@Composable
private fun TotalsCard(ingredient: Ingredient, totals: StockTotals) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("За всё время", style = MaterialTheme.typography.titleMedium)
            PriceRow("Поступило", ingredient.format(totals.received))
            PriceRow("Ушло в заказы", ingredient.format(totals.usedInOrders))
            if (totals.returned > 0) PriceRow("Вернулось после отмен", ingredient.format(totals.returned))
            PriceRow("Списано", ingredient.format(totals.writtenOff), color = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
private fun UsagesCard(ingredient: Ingredient, usages: List<DishUsage>, onOpenDish: (Int) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(vertical = 4.dp)) {
            usages.forEach { usage ->
                val portions = if (usage.amount > 0) (ingredient.stock / usage.amount).toInt() else 0
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenDish(usage.menuItemId) }
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(usage.name, style = MaterialTheme.typography.bodyLarge)
                        Text(
                            "${usage.type.title} · хватит на $portions порц.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(ingredient.format(usage.amount), style = MaterialTheme.typography.titleSmall)
                }
            }
        }
    }
}
