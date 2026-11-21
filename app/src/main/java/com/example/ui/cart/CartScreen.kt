package com.example.ui.cart

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.R
import com.example.model.OrderLine
import com.example.model.formatPrice
import com.example.service.Pizzeria
import com.example.ui.AppViewModelProvider
import com.example.ui.components.CartLineCard
import com.example.ui.components.EmptyState
import com.example.ui.components.LoadingState
import com.example.ui.components.PizzeriaTopBar
import com.example.ui.components.PriceBreakdown
import com.example.ui.components.PromoCodeField
import com.example.ui.components.SectionTitle
import com.example.ui.components.SummaryBar
import com.example.ui.components.WarningCard

@Composable
fun CartScreen(
    onCheckout: () -> Unit,
    onOpenMenu: () -> Unit,
    viewModel: CartViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    CartContent(
        state = state,
        onIncrease = viewModel::increase,
        onDecrease = viewModel::decrease,
        onDelete = viewModel::remove,
        onApplyPromo = viewModel::applyPromo,
        onRemovePromo = viewModel::removePromo,
        onCheckout = onCheckout,
        onOpenMenu = onOpenMenu
    )
}

@Composable
fun CartContent(
    state: CartUiState,
    onIncrease: (OrderLine) -> Unit,
    onDecrease: (OrderLine) -> Unit,
    onDelete: (OrderLine) -> Unit,
    onApplyPromo: (String) -> Unit,
    onRemovePromo: () -> Unit,
    onCheckout: () -> Unit,
    onOpenMenu: () -> Unit
) {
    Scaffold(
        topBar = { PizzeriaTopBar(title = "Корзина") },
        bottomBar = {
            if (state.lines.isNotEmpty()) {
                SummaryBar(
                    label = if (state.shortages.isEmpty()) "${state.count} шт." else "Не хватает ингредиентов",
                    total = state.quote.subtotal - state.quote.discount,
                    actionText = "Оформить",
                    onAction = onCheckout,
                    enabled = state.shortages.isEmpty()
                )
            }
        }
    ) { padding ->
        if (state.isLoading) {
            LoadingState(Modifier.padding(padding))
            return@Scaffold
        }
        if (state.lines.isEmpty()) {
            EmptyState(
                icon = R.drawable.ic_cart,
                title = "Корзина пуста",
                subtitle = "Выберите пиццу, напиток или десерт в меню",
                actionText = "Перейти в меню",
                onAction = onOpenMenu,
                modifier = Modifier.padding(padding)
            )
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (state.shortages.isNotEmpty()) {
                item(key = "shortages") {
                    WarningCard(
                        title = "На складе не хватает ингредиентов. Уменьшите количество:",
                        lines = state.shortages.map { it.describe() }
                    )
                }
            }
            items(state.lines, key = { it.key }) { line ->
                CartLineCard(
                    line = line,
                    onIncrease = { onIncrease(line) },
                    onDecrease = { onDecrease(line) },
                    onDelete = { onDelete(line) },
                    modifier = Modifier.animateItem()
                )
            }
            item {
                PromoCodeField(
                    applied = state.promo,
                    error = state.promoError,
                    hint = state.promoHint,
                    onApply = onApplyPromo,
                    onRemove = onRemovePromo
                )
            }
            item {
                Text(
                    text = "Попробуйте STUDENT10, ROMA5 или FREESHIP",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            item { SectionTitle("Сумма") }
            item { PriceBreakdown(quote = state.quote, showDelivery = false, promoCode = state.promo?.code) }
            item {
                Text(
                    text = "Доставка ${Pizzeria.DELIVERY_FEE.formatPrice()}, бесплатно от ${Pizzeria.FREE_DELIVERY_FROM.formatPrice()}. Самовывоз всегда бесплатный",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
