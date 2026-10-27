package com.example.ui.checkout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.R
import com.example.model.DeliveryType
import com.example.ui.AppViewModelProvider
import com.example.ui.components.EmptyState
import com.example.ui.components.OrderLineRow
import com.example.ui.components.PizzeriaTopBar
import com.example.ui.components.PriceBreakdown
import com.example.ui.components.SectionTitle
import com.example.ui.components.SummaryBar

@Composable
fun CheckoutScreen(
    onBack: () -> Unit,
    onOrderPlaced: (Int) -> Unit,
    viewModel: CheckoutViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    CheckoutContent(
        state = state,
        onBack = onBack,
        onDeliveryTypeChange = viewModel::selectDeliveryType,
        onPlaceOrder = { form -> viewModel.placeOrder(form)?.let(onOrderPlaced) }
    )
}

@Composable
fun CheckoutContent(
    state: CheckoutUiState,
    onBack: () -> Unit,
    onDeliveryTypeChange: (DeliveryType) -> Unit,
    onPlaceOrder: (CheckoutForm) -> Unit
) {
    var name by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var address by rememberSaveable { mutableStateOf("") }
    var comment by rememberSaveable { mutableStateOf("") }
    var submitted by rememberSaveable { mutableStateOf(false) }

    val form = CheckoutForm(name, phone, address, comment)
    val errors = if (submitted) form.errors(state.deliveryType) else CheckoutErrors()

    Scaffold(
        topBar = { PizzeriaTopBar(title = "Оформление", onBack = onBack) },
        bottomBar = {
            if (state.lines.isNotEmpty()) {
                SummaryBar(
                    label = "К оплате",
                    total = state.quote.total,
                    actionText = "Заказать",
                    onAction = {
                        submitted = true
                        onPlaceOrder(form)
                    }
                )
            }
        }
    ) { padding ->
        if (state.lines.isEmpty()) {
            EmptyState(
                icon = R.drawable.ic_cart,
                title = "Корзина пуста",
                subtitle = "Нечего оформлять",
                actionText = "Назад",
                onAction = onBack,
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
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                DeliveryType.entries.forEachIndexed { index, type ->
                    SegmentedButton(
                        selected = state.deliveryType == type,
                        onClick = { onDeliveryTypeChange(type) },
                        shape = SegmentedButtonDefaults.itemShape(index, DeliveryType.entries.size),
                        label = { Text(type.title) }
                    )
                }
            }

            SectionTitle("Контакты")
            FormField(
                value = name,
                onValueChange = { name = it },
                label = "Имя",
                error = errors.name,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next)
            )
            FormField(
                value = phone,
                onValueChange = { phone = it },
                label = "Телефон",
                error = errors.phone,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next)
            )
            if (state.deliveryType == DeliveryType.DELIVERY) {
                FormField(
                    value = address,
                    onValueChange = { address = it },
                    label = "Адрес доставки",
                    error = errors.address,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )
            } else {
                Text(
                    text = "Забрать заказ: ${CheckoutForm.PICKUP_ADDRESS}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            FormField(
                value = comment,
                onValueChange = { comment = it },
                label = "Комментарий к заказу",
                singleLine = false
            )

            SectionTitle("Ваш заказ")
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    state.lines.forEach { OrderLineRow(it) }
                    PriceBreakdown(quote = state.quote, promoCode = state.promoCode, modifier = Modifier.padding(top = 8.dp))
                }
            }
        }
    }
}

@Composable
private fun FormField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    error: String? = null,
    singleLine: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        isError = error != null,
        supportingText = error?.let { { Text(it) } },
        singleLine = singleLine,
        minLines = if (singleLine) 1 else 2,
        keyboardOptions = keyboardOptions
    )
}
