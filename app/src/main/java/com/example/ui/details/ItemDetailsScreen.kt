package com.example.ui.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.R
import com.example.model.Extra
import com.example.model.Pizza
import com.example.model.PizzaSize
import com.example.model.formatPrice
import com.example.ui.AppViewModelProvider
import com.example.ui.components.EmptyState
import com.example.ui.components.FavoriteButton
import com.example.ui.components.ItemLabels
import com.example.ui.components.LabeledCheckbox
import com.example.ui.components.MenuItemImage
import com.example.ui.components.PizzeriaTopBar
import com.example.ui.components.QuantityStepper
import com.example.ui.components.SectionTitle
import com.example.ui.components.SummaryBar

@Composable
fun ItemDetailsScreen(
    onBack: () -> Unit,
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
        onAddToCart = {
            viewModel.addToCart()
            onBack()
        }
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
    onAddToCart: () -> Unit
) {
    val item = state.item
    Scaffold(
        topBar = {
            PizzeriaTopBar(title = item?.name ?: "Блюдо", onBack = onBack) {
                if (item != null) FavoriteButton(isFavorite = state.isFavorite, onToggle = onToggleFavorite)
            }
        },
        bottomBar = {
            if (item != null) {
                SummaryBar(
                    label = if (state.inCart > 0) "Уже в корзине: ${state.inCart} шт." else "Итого",
                    total = state.total,
                    actionText = "Добавить в корзину",
                    onAction = onAddToCart
                )
            }
        }
    ) { padding ->
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
            }
        }
    }
}
