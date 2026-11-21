package com.example.ui.inventory

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.R
import com.example.model.StockUnit
import com.example.ui.AppViewModelProvider
import com.example.ui.common.AppSnackbarHost
import com.example.ui.components.ChoiceRow
import com.example.ui.components.EmptyState
import com.example.ui.components.FormTextField
import com.example.ui.components.LoadingState
import com.example.ui.components.PizzeriaTopBar
import com.example.ui.components.SectionTitle

@Composable
fun IngredientEditScreen(
    onBack: () -> Unit,
    onSaved: (Int) -> Unit,
    viewModel: IngredientEditViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    IngredientEditContent(
        state = state,
        onBack = onBack,
        onFormChange = viewModel::onFormChange,
        onSave = { viewModel.save(onSaved) }
    )
}

@Composable
fun IngredientEditContent(
    state: IngredientEditUiState,
    onBack: () -> Unit,
    onFormChange: (IngredientForm) -> Unit,
    onSave: () -> Unit
) {
    val form = state.form
    val errors = state.errors
    Scaffold(
        snackbarHost = { AppSnackbarHost() },
        topBar = { PizzeriaTopBar(title = if (state.isNew) "Новый ингредиент" else "Редактирование", onBack = onBack) },
        bottomBar = {
            if (!state.isLoading && !state.notFound) {
                Row(modifier = Modifier.navigationBarsPadding().padding(16.dp)) {
                    Button(onClick = onSave, enabled = !state.isSaving, modifier = Modifier.fillMaxWidth()) {
                        Text(if (state.isNew) "Добавить на склад" else "Сохранить")
                    }
                }
            }
        }
    ) { padding ->
        when {
            state.isLoading -> {
                LoadingState(Modifier.padding(padding))
                return@Scaffold
            }
            state.notFound -> {
                EmptyState(
                    icon = R.drawable.ic_inventory,
                    title = "Ингредиент не найден",
                    subtitle = "Возможно, его уже удалили",
                    actionText = "Назад",
                    onAction = onBack,
                    modifier = Modifier.padding(padding)
                )
                return@Scaffold
            }
        }

        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FormTextField(
                value = form.name,
                onValueChange = { onFormChange(form.copy(name = it)) },
                label = "Название",
                error = errors.name,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Next)
            )

            SectionTitle("Единица измерения")
            ChoiceRow(
                options = StockUnit.entries,
                selected = form.unit,
                label = { it.short },
                onSelect = { onFormChange(form.copy(unit = it)) },
                enabled = state.isNew
            )
            Text(
                text = if (state.isNew) form.unit.title else "Единицу нельзя изменить: на неё опирается история операций",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            SectionTitle("Количество")
            if (state.isNew) {
                FormTextField(
                    value = form.stock,
                    onValueChange = { onFormChange(form.copy(stock = it)) },
                    label = "Начальный остаток",
                    suffix = form.unit.short,
                    error = errors.stock,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next)
                )
            } else {
                Text(
                    "Остаток: ${form.stock} ${form.unit.short}. Он меняется только через поставки, списания и заказы",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            FormTextField(
                value = form.lowThreshold,
                onValueChange = { onFormChange(form.copy(lowThreshold = it)) },
                label = "Предупреждать, когда останется",
                suffix = form.unit.short,
                error = errors.lowThreshold,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done)
            )
        }
    }
}
