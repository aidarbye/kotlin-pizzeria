package com.example.ui.orders

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.R
import com.example.model.DeliveryType
import com.example.model.Order
import com.example.model.OrderStatus
import com.example.model.progress
import com.example.model.toText
import com.example.ui.AppViewModelProvider
import com.example.ui.components.ConfirmDialog
import com.example.ui.components.EmptyState
import com.example.ui.components.LoadingState
import com.example.ui.components.OrderLineRow
import com.example.ui.components.PizzeriaTopBar
import com.example.ui.components.PriceBreakdown
import com.example.ui.components.PriceRow
import com.example.ui.components.SectionTitle
import com.example.ui.components.StatusTimeline
import com.example.ui.components.formatTime

@Composable
fun OrderDetailsScreen(
    onBack: () -> Unit,
    onRepeated: () -> Unit,
    viewModel: OrderDetailsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    OrderDetailsContent(
        orderId = viewModel.orderId,
        order = state.order,
        isLoading = state.isLoading,
        onBack = onBack,
        onCancel = viewModel::cancel,
        onRepeat = { viewModel.repeat(onRepeated = onRepeated) }
    )
}

@Composable
fun OrderDetailsContent(
    orderId: Int,
    order: Order?,
    isLoading: Boolean,
    onBack: () -> Unit,
    onCancel: () -> Unit,
    onRepeat: () -> Unit
) {
    var confirmCancel by rememberSaveable { mutableStateOf(false) }

    Scaffold(topBar = { PizzeriaTopBar(title = "Заказ #$orderId", onBack = onBack) }) { padding ->
        if (isLoading) {
            LoadingState(Modifier.padding(padding))
            return@Scaffold
        }
        if (order == null) {
            EmptyState(
                icon = R.drawable.ic_orders,
                title = "Заказ не найден",
                subtitle = "Такого заказа нет в истории",
                modifier = Modifier.padding(padding)
            )
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(order.status.toText(order.deliveryType), style = MaterialTheme.typography.titleLarge)
                    if (order.status is OrderStatus.Cancelled) {
                        Text(
                            "Деньги вернутся на карту в течение дня",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        StatusTimeline(status = order.status, deliveryType = order.deliveryType)
                        if (order.isActive) {
                            LinearProgressIndicator(
                                progress = { order.status.progress() },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            SectionTitle("Состав")
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    order.lines.forEach { OrderLineRow(it) }
                    PriceBreakdown(quote = order.quote, promoCode = order.promoCode, modifier = Modifier.padding(top = 8.dp))
                }
            }

            SectionTitle("Получение")
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    PriceRow("Способ", order.deliveryType.title)
                    PriceRow("Оформлен", order.createdAt.formatTime())
                    PriceRow("Имя", order.customer.name)
                    PriceRow("Телефон", order.customer.phone)
                    PriceRow(if (order.deliveryType == DeliveryType.DELIVERY) "Адрес" else "Пиццерия", order.customer.address)
                    if (order.comment.isNotEmpty()) PriceRow("Комментарий", order.comment)
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (order.canCancel) {
                    OutlinedButton(onClick = { confirmCancel = true }, modifier = Modifier.weight(1f)) {
                        Text("Отменить")
                    }
                }
                Button(onClick = onRepeat, modifier = Modifier.weight(1f)) { Text("Повторить заказ") }
            }
        }
    }

    if (confirmCancel && order?.canCancel == true) {
        ConfirmDialog(
            title = "Отменить заказ #$orderId?",
            text = "Отменить можно, пока заказ не передан курьеру. Ингредиенты вернутся на склад.",
            confirmText = "Отменить заказ",
            dismissText = "Нет",
            onConfirm = {
                confirmCancel = false
                onCancel()
            },
            onDismiss = { confirmCancel = false }
        )
    }
}
