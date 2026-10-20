package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.formatPrice

@Composable
fun CheckoutDialog(
    total: Double,
    onDismiss: () -> Unit,
    onConfirm: (name: String, address: String, studentDiscount: Boolean) -> Unit
) {
    var name by rememberSaveable { mutableStateOf("") }
    var address by rememberSaveable { mutableStateOf("") }
    var studentDiscount by rememberSaveable { mutableStateOf(false) }
    val toPay = if (studentDiscount) total * 0.9 else total

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Оформление заказа") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Имя") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Адрес доставки") },
                    singleLine = true
                )
                LabeledCheckbox(
                    text = "Студенческая скидка 10%",
                    checked = studentDiscount,
                    onCheckedChange = { studentDiscount = it }
                )
                Text(
                    text = "К оплате: ${toPay.formatPrice()}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(name, address, studentDiscount) },
                enabled = name.isNotBlank() && address.isNotBlank()
            ) { Text("Заказать") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        }
    )
}
