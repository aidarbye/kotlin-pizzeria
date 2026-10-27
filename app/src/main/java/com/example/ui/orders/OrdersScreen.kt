package com.example.ui.orders

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.R
import com.example.ui.AppViewModelProvider
import com.example.ui.components.EmptyState
import com.example.ui.components.OrderCard
import com.example.ui.components.PizzeriaTopBar
import com.example.ui.components.SectionTitle
import com.example.ui.components.StatsCard

@Composable
fun OrdersScreen(
    onOpenOrder: (Int) -> Unit,
    onOpenMenu: () -> Unit,
    viewModel: OrdersViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    OrdersContent(state = state, onOpenOrder = onOpenOrder, onOpenMenu = onOpenMenu)
}

@Composable
fun OrdersContent(
    state: OrdersUiState,
    onOpenOrder: (Int) -> Unit,
    onOpenMenu: () -> Unit
) {
    Scaffold(topBar = { PizzeriaTopBar(title = "Мои заказы") }) { padding ->
        if (state.isEmpty) {
            EmptyState(
                icon = R.drawable.ic_orders,
                title = "Заказов пока нет",
                subtitle = "Здесь появятся ваши заказы и их статус в реальном времени",
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
            if (state.active.isNotEmpty()) {
                item { SectionTitle("Активные") }
                items(state.active, key = { it.id }) { order ->
                    OrderCard(order = order, onClick = { onOpenOrder(order.id) }, modifier = Modifier.animateItem())
                }
            }
            if (state.history.isNotEmpty()) {
                item { SectionTitle("История") }
                items(state.history, key = { it.id }) { order ->
                    OrderCard(order = order, onClick = { onOpenOrder(order.id) }, modifier = Modifier.animateItem())
                }
            }
            item { StatsCard(revenue = state.revenue, popularity = state.popularity) }
        }
    }
}
