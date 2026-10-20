package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.Order
import com.example.model.OrderStatus
import com.example.model.formatPrice
import com.example.model.progress
import com.example.model.toText

@Composable
fun StatusLabel(status: OrderStatus, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val (container, content) = when (status) {
        is OrderStatus.Cancelled -> colors.errorContainer to colors.onErrorContainer
        is OrderStatus.Delivered -> colors.tertiaryContainer to colors.onTertiaryContainer
        else -> colors.secondaryContainer to colors.onSecondaryContainer
    }
    Surface(modifier = modifier, shape = CircleShape, color = container) {
        Text(
            text = status.toText(),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelMedium,
            color = content
        )
    }
}

@Composable
fun OrderCard(
    order: Order,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Заказ #${order.id}",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                StatusLabel(order.status)
            }
            Text(
                text = "${order.customer.name} · ${order.customer.address}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = order.lines.joinToString { "${it.item.name} × ${it.quantity}" },
                style = MaterialTheme.typography.bodySmall
            )
            if (order.status !is OrderStatus.Cancelled) {
                LinearProgressIndicator(
                    progress = { order.status.progress() },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = order.paid.formatPrice(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                if (order.paid < order.total()) {
                    Text(
                        text = "скидка 10%",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                    Spacer(Modifier.padding(4.dp))
                }
                if (order.isActive) {
                    TextButton(onClick = onCancel) { Text("Отменить") }
                }
            }
        }
    }
}

@Composable
fun StatsCard(
    revenue: Double,
    popularity: Map<String, Int>,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Выручка", style = MaterialTheme.typography.labelLarge)
            Text(revenue.formatPrice(), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            if (popularity.isNotEmpty()) {
                HorizontalDivider(Modifier.padding(vertical = 8.dp))
                Text("Популярное", style = MaterialTheme.typography.labelLarge)
                popularity.entries.sortedByDescending { it.value }.take(3).forEach { (name, count) ->
                    Row {
                        Text(name, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                        Text("$count шт.", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}
