package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.Ingredient
import com.example.model.StockTransaction
import com.example.model.TransactionType
import com.example.ui.inventory.StockOperation
import com.example.ui.inventory.validateStockAmount
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

fun Long.formatDateTime(): String = SimpleDateFormat("dd.MM HH:mm", Locale.getDefault()).format(Date(this))

/** Label with the stock level: "Нет", "Мало" or nothing when there is enough. */
@Composable
fun StockLabel(ingredient: Ingredient, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    when {
        ingredient.isOut -> InfoLabel("Нет в наличии", colors.errorContainer, colors.onErrorContainer, modifier)
        ingredient.isLow -> InfoLabel("Заканчивается", colors.secondaryContainer, colors.onSecondaryContainer, modifier)
    }
}

/** Stock compared with twice the low threshold, so "low" is in the first half of the bar. */
@Composable
fun StockLevelBar(ingredient: Ingredient, modifier: Modifier = Modifier) {
    val full = (ingredient.lowThreshold * 2).takeIf { it > 0 } ?: ingredient.stock.coerceAtLeast(1.0)
    val color = when {
        ingredient.isOut || ingredient.isLow -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.primary
    }
    LinearProgressIndicator(
        progress = { (ingredient.stock / full).toFloat().coerceIn(0f, 1f) },
        modifier = modifier.fillMaxWidth(),
        color = color
    )
}

@Composable
fun IngredientCard(
    ingredient: Ingredient,
    usedIn: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    ingredient.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                StockLabel(ingredient)
            }
            StockLevelBar(ingredient)
            Row {
                Text(
                    text = ingredient.format(ingredient.stock),
                    style = MaterialTheme.typography.titleSmall,
                    color = if (ingredient.isOut || ingredient.isLow) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = if (usedIn > 0) "в $usedIn блюд." else "не используется",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun StatTile(value: String, label: String, modifier: Modifier = Modifier, color: Color = Color.Unspecified) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(12.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = color)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun transactionColor(type: TransactionType): Color = when (type) {
    TransactionType.RESTOCK, TransactionType.RETURN -> MaterialTheme.colorScheme.tertiary
    TransactionType.ORDER -> MaterialTheme.colorScheme.primary
    TransactionType.WRITE_OFF -> MaterialTheme.colorScheme.error
}

@Composable
fun TransactionRow(
    transaction: StockTransaction,
    ingredient: Ingredient,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(
            painterResource(R.drawable.ic_history),
            contentDescription = null,
            tint = transactionColor(transaction.type)
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(transaction.type.title, style = MaterialTheme.typography.titleSmall)
            Text(
                text = listOf(transaction.note, transaction.createdAt.formatDateTime()).filter { it.isNotBlank() }.joinToString(" · "),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = (if (transaction.delta >= 0) "+" else "−") + ingredient.format(abs(transaction.delta)),
            style = MaterialTheme.typography.titleSmall,
            color = transactionColor(transaction.type)
        )
    }
}

/** Dialog for a restock or a write-off. Typed values survive rotation; validation runs on submit. */
@Composable
fun StockOperationDialog(
    operation: StockOperation,
    ingredient: Ingredient,
    onConfirm: (amount: String, note: String) -> Unit,
    onDismiss: () -> Unit
) {
    var amount by rememberSaveable { mutableStateOf("") }
    var note by rememberSaveable { mutableStateOf("") }
    var submitted by rememberSaveable { mutableStateOf(false) }
    val errors = validateStockAmount(amount, operation, ingredient, note)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("${operation.title}: ${ingredient.name}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    "Сейчас на складе: ${ingredient.format(ingredient.stock)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                FormTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = "Количество",
                    suffix = ingredient.unit.short,
                    error = errors.amount.takeIf { submitted },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next)
                )
                FormTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = if (operation == StockOperation.WRITE_OFF) "Причина (испортилось, брак...)" else "Комментарий (поставщик)",
                    error = errors.note.takeIf { submitted }
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                submitted = true
                if (errors.isValid) onConfirm(amount, note)
            }) { Text(operation.action) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } }
    )
}
