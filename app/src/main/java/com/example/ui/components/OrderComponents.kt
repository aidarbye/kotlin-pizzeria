package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.DeliveryType
import com.example.model.Order
import com.example.model.OrderLine
import com.example.model.OrderStatus
import com.example.model.formatPrice
import com.example.model.progress
import com.example.model.stepIndex
import com.example.model.steps
import com.example.model.toText
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun Long.formatTime(): String = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(this))

@Composable
fun StatusLabel(status: OrderStatus, deliveryType: DeliveryType, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val (container, content) = when (status) {
        is OrderStatus.Cancelled -> colors.errorContainer to colors.onErrorContainer
        is OrderStatus.Delivered -> colors.tertiaryContainer to colors.onTertiaryContainer
        else -> colors.secondaryContainer to colors.onSecondaryContainer
    }
    InfoLabel(text = status.toText(deliveryType), container = container, content = content, modifier = modifier)
}

@Composable
fun OrderCard(
    order: Order,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(onClick = onClick, modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Заказ #${order.id} · ${order.createdAt.formatTime()}",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                StatusLabel(order.status, order.deliveryType)
            }
            Text(
                text = order.lines.joinToString { "${it.item.name} × ${it.quantity}" },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (order.isActive) {
                LinearProgressIndicator(
                    progress = { order.status.progress() },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = order.deliveryType.title,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = order.quote.total.formatPrice(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun StatsCard(
    revenue: Double,
    popularity: List<Pair<String, Int>>,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Потрачено всего", style = MaterialTheme.typography.labelLarge)
            Text(revenue.formatPrice(), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            if (popularity.isNotEmpty()) {
                HorizontalDivider(Modifier.padding(vertical = 8.dp))
                Text("Чаще всего заказываете", style = MaterialTheme.typography.labelLarge)
                popularity.forEach { (name, count) ->
                    PriceRow(label = name, value = "$count шт.")
                }
            }
        }
    }
}

@Composable
fun StatusTimeline(
    status: OrderStatus,
    deliveryType: DeliveryType,
    modifier: Modifier = Modifier
) {
    val steps = deliveryType.steps()
    val current = if (status is OrderStatus.Delivered) steps.lastIndex else status.stepIndex()
    val colors = MaterialTheme.colorScheme

    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        steps.forEachIndexed { index, title ->
            val done = current >= 0 && index <= current
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .weight(1f)
                            .height(2.dp)
                            .background(if (index == 0) Color.Transparent else if (done) colors.primary else colors.outlineVariant)
                    )
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(if (done) colors.primary else colors.surfaceContainerHighest, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (done) {
                            Icon(
                                painterResource(R.drawable.ic_check),
                                contentDescription = null,
                                tint = colors.onPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        } else {
                            Text("${index + 1}", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                    Box(
                        Modifier
                            .weight(1f)
                            .height(2.dp)
                            .background(
                                if (index == steps.lastIndex) Color.Transparent
                                else if (current > index) colors.primary else colors.outlineVariant
                            )
                    )
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Center,
                    color = if (done) colors.onSurface else colors.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
fun OrderLineRow(line: OrderLine, modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text("${line.item.name} × ${line.quantity}", style = MaterialTheme.typography.bodyMedium)
            val options = line.options()
            if (options.isNotEmpty()) {
                Text(options, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Text(line.subtotal.formatPrice(), style = MaterialTheme.typography.bodyMedium)
    }
}
