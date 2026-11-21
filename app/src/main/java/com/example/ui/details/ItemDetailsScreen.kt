package com.example.ui.details

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
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
import com.example.model.Extra
import com.example.model.Ingredient
import com.example.model.Pizza
import com.example.model.PizzaSize
import com.example.model.formatPrice
import com.example.ui.AppViewModelProvider
import com.example.ui.components.AvailabilityLabel
import com.example.ui.components.ConfirmDialog
import com.example.ui.components.EmptyState
import com.example.ui.components.FavoriteButton
import com.example.ui.components.ItemLabels
import com.example.ui.components.LabeledCheckbox
import com.example.ui.components.LoadingState
import com.example.ui.components.MenuItemImage
import com.example.ui.components.PizzeriaTopBar
import com.example.ui.components.QuantityStepper
import com.example.ui.components.SectionTitle
import com.example.ui.components.SummaryBar

@Composable
fun ItemDetailsScreen(
    onBack: () -> Unit,
    onEdit: (Int) -> Unit,
    onOpenIngredient: (Int) -> Unit,
    viewModel: ItemDetailsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    ItemDetailsContent(
        state = state,
        onBack = onBack,
        onToggleFavorite = viewModel::toggleFavorite,
        onSelectSize = viewModel::selectSize,
        onToggleExtra = viewModel::toggleExtra,
        onChangeQuantity = viewModel::changeQuantity,
        onAddToCart = { viewModel.addToCart(onAdded = onBack) },
        onEdit = { onEdit(viewModel.itemId) },
        onDelete = { viewModel.delete(onDeleted = onBack) },
        onOpenIngredient = onOpenIngredient
    )
}

@Composable
fun ItemDetailsContent(
    state: ItemDetailsUiState,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit,
    onSelectSize: (PizzaSize) -> Unit,
    onToggleExtra: (Extra) -> Unit,
    onChangeQuantity: (Int) -> Unit,
    onAddToCart: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onOpenIngredient: (Int) -> Unit
) {
    val item = state.item
    var confirmDelete by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            PizzeriaTopBar(title = item?.name ?: "Блюдо", onBack = onBack) {
                if (item != null) {
                    FavoriteButton(isFavorite = state.isFavorite, onToggle = onToggleFavorite)
                    IconButton(onClick = onEdit) {
                        Icon(painterResource(R.drawable.ic_edit), contentDescription = "Редактировать")
                    }
                    IconButton(onClick = { confirmDelete = true }) {
                        Icon(painterResource(R.drawable.ic_delete), contentDescription = "Удалить")
                    }
                }
            }
        },
        bottomBar = {
            if (item != null) {
                SummaryBar(
                    label = when {
                        !state.canAdd && state.portionsLeft == 0 -> "Нет в наличии"
                        !state.canAdd -> "На складе хватит на ${state.portionsLeft} шт."
                        state.inCart > 0 -> "Уже в корзине: ${state.inCart} шт."
                        else -> "Итого"
                    },
                    total = state.total,
                    actionText = "Добавить в корзину",
                    onAction = onAddToCart,
                    enabled = state.canAdd
                )
            }
        }
    ) { padding ->
        if (state.isLoading) {
            LoadingState(Modifier.padding(padding))
            return@Scaffold
        }
        if (item == null) {
            EmptyState(
                icon = R.drawable.ic_pizza,
                title = "Блюдо не найдено",
                subtitle = "Возможно, его убрали из меню",
                modifier = Modifier.padding(padding)
            )
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MenuItemImage(item, size = 140.dp, modifier = Modifier.align(Alignment.CenterHorizontally))
            Text(item.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            ItemLabels(item)
            Text(item.description, style = MaterialTheme.typography.bodyLarge)
            Text(item.details(), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

            if (item is Pizza) {
                SectionTitle("Размер")
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    PizzaSize.entries.forEachIndexed { index, size ->
                        SegmentedButton(
                            selected = state.size == size,
                            onClick = { onSelectSize(size) },
                            shape = SegmentedButtonDefaults.itemShape(index, PizzaSize.entries.size),
                            label = { Text("${size.cm} см") }
                        )
                    }
                }
                Text(
                    text = "${state.size.title}: ${(item.price * state.size.priceFactor).formatPrice()}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                SectionTitle("Добавки")
                state.availableExtras.forEach { extra ->
                    LabeledCheckbox(
                        text = extra.name,
                        checked = extra in state.selectedExtras,
                        onCheckedChange = { onToggleExtra(extra) },
                        trailing = "+${extra.price.formatPrice()}"
                    )
                }
            }

            SectionTitle("Количество")
            Row(verticalAlignment = Alignment.CenterVertically) {
                QuantityStepper(
                    quantity = state.quantity,
                    onAdd = { onChangeQuantity(1) },
                    onRemove = { onChangeQuantity(-1) }
                )
                Spacer(Modifier.width(12.dp))
                AvailabilityLabel(state.portionsLeft)
            }

            SectionTitle(if (state.isPizza) "Рецепт (${state.size.cm} см)" else "Рецепт")
            if (state.recipe.isEmpty()) {
                Text(
                    "Ингредиенты не указаны, склад для этого блюда не учитывается",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        state.recipe.forEach { row ->
                            RecipeRowItem(row = row, onClick = { onOpenIngredient(row.ingredient.id) })
                        }
                    }
                }
            }
        }
    }

    if (confirmDelete && item != null) {
        ConfirmDialog(
            title = "Удалить «${item.name}»?",
            text = "Блюдо исчезнет из меню и из корзины. Старые заказы с ним останутся в истории.",
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
private fun RecipeRowItem(row: RecipeRow, onClick: () -> Unit) {
    val ingredient = row.ingredient
    val enough = ingredient.stock + Ingredient.EPSILON >= row.amount
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(ingredient.name, style = MaterialTheme.typography.bodyLarge)
            Text(
                "На складе: ${ingredient.format(ingredient.stock)}",
                style = MaterialTheme.typography.bodySmall,
                color = if (enough && !ingredient.isLow) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.error
            )
        }
        Text(ingredient.format(row.amount), style = MaterialTheme.typography.titleSmall)
    }
}
