package com.example.ui.editor

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.R
import com.example.data.SampleData
import com.example.model.DishType
import com.example.model.Ingredient
import com.example.ui.AppViewModelProvider
import com.example.ui.components.ChoiceRow
import com.example.ui.components.EmptyState
import com.example.ui.components.FormTextField
import com.example.ui.components.LabeledCheckbox
import com.example.ui.components.LoadingState
import com.example.ui.components.PizzeriaTopBar
import com.example.ui.components.SectionTitle
import com.example.ui.components.SummaryBar
import com.example.ui.theme.PizzeriaTheme

@Composable
fun MenuItemEditScreen(
    onBack: () -> Unit,
    onSaved: (Int) -> Unit,
    viewModel: MenuItemEditViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    MenuItemEditContent(
        state = state,
        onBack = onBack,
        onFormChange = viewModel::onFormChange,
        onAddIngredient = viewModel::addIngredient,
        onAmountChange = viewModel::changeAmount,
        onRemoveIngredient = viewModel::removeIngredient,
        onSave = { viewModel.save(onSaved) }
    )
}

@Composable
fun MenuItemEditContent(
    state: MenuItemEditUiState,
    onBack: () -> Unit,
    onFormChange: (MenuItemForm) -> Unit,
    onAddIngredient: (Ingredient) -> Unit,
    onAmountChange: (ingredientId: Int, amount: String) -> Unit,
    onRemoveIngredient: (Int) -> Unit,
    onSave: () -> Unit
) {
    val form = state.form
    val errors = state.errors
    var pickIngredient by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = { PizzeriaTopBar(title = if (state.isNew) "Новое блюдо" else "Редактирование", onBack = onBack) },
        bottomBar = {
            if (!state.isLoading && !state.notFound) {
                SummaryBar(
                    label = if (state.isNew) "Цена" else "Новая цена",
                    total = form.price.toDecimal() ?: 0.0,
                    actionText = if (state.isNew) "Добавить в меню" else "Сохранить",
                    onAction = onSave,
                    enabled = !state.isSaving
                )
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
                    icon = R.drawable.ic_pizza,
                    title = "Блюдо не найдено",
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
            SectionTitle("Тип")
            ChoiceRow(
                options = DishType.entries,
                selected = form.type,
                label = { it.title },
                onSelect = { onFormChange(form.copy(type = it)) },
                enabled = state.isNew
            )
            if (!state.isNew) {
                Text(
                    "Тип блюда нельзя изменить после создания",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            SectionTitle("Основное")
            FormTextField(
                value = form.name,
                onValueChange = { onFormChange(form.copy(name = it)) },
                label = "Название",
                error = errors.name,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Next)
            )
            FormTextField(
                value = form.price,
                onValueChange = { onFormChange(form.copy(price = it)) },
                label = if (form.type == DishType.PIZZA) "Цена за 30 см" else "Цена",
                suffix = "$",
                error = errors.price,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next)
            )
            FormTextField(
                value = form.description,
                onValueChange = { onFormChange(form.copy(description = it)) },
                label = "Описание",
                error = errors.description,
                singleLine = false,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
            )

            when (form.type) {
                DishType.PIZZA -> {
                    FormTextField(
                        value = form.toppings,
                        onValueChange = { onFormChange(form.copy(toppings = it)) },
                        label = "Начинка через запятую",
                        error = errors.toppings
                    )
                    LabeledCheckbox("Вегетарианская", form.isVegetarian, { onFormChange(form.copy(isVegetarian = it)) })
                    LabeledCheckbox("Острая", form.isSpicy, { onFormChange(form.copy(isSpicy = it)) })
                }
                DishType.DRINK -> FormTextField(
                    value = form.volumeMl,
                    onValueChange = { onFormChange(form.copy(volumeMl = it)) },
                    label = "Объём",
                    suffix = "мл",
                    error = errors.volume,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                DishType.DESSERT ->
                    LabeledCheckbox("Без сахара", form.isSugarFree, { onFormChange(form.copy(isSugarFree = it)) })
            }

            SectionTitle("Рецепт")
            Text(
                text = if (form.type == DishType.PIZZA) {
                    "Сколько ингредиентов уходит на одну пиццу 30 см. Для 25 и 35 см количество пересчитывается"
                } else {
                    "Сколько ингредиентов уходит на одну порцию. При заказе они списываются со склада"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            val ingredientsById = state.ingredients.associateBy { it.id }
            form.recipe.forEach { input ->
                val ingredient = ingredientsById[input.ingredientId] ?: return@forEach
                RecipeInputRow(
                    ingredient = ingredient,
                    amount = input.amount,
                    error = errors.recipe[input.ingredientId],
                    onAmountChange = { onAmountChange(ingredient.id, it) },
                    onRemove = { onRemoveIngredient(ingredient.id) }
                )
            }
            if (form.recipe.isEmpty()) {
                Text(
                    "Без рецепта склад для блюда не учитывается",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            OutlinedButton(onClick = { pickIngredient = true }, modifier = Modifier.fillMaxWidth()) {
                Icon(painterResource(R.drawable.ic_add), contentDescription = null)
                Text("Добавить ингредиент", modifier = Modifier.padding(start = 8.dp))
            }
        }
    }

    if (pickIngredient) {
        val used = form.recipe.map { it.ingredientId }.toSet()
        IngredientPickerDialog(
            ingredients = state.ingredients.filter { it.id !in used },
            onPick = {
                onAddIngredient(it)
                pickIngredient = false
            },
            onDismiss = { pickIngredient = false }
        )
    }
}

@Composable
private fun RecipeInputRow(
    ingredient: Ingredient,
    amount: String,
    error: String?,
    onAmountChange: (String) -> Unit,
    onRemove: () -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        FormTextField(
            value = amount,
            onValueChange = onAmountChange,
            label = ingredient.name,
            suffix = ingredient.unit.short,
            error = error,
            modifier = Modifier.weight(1f),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next)
        )
        IconButton(onClick = onRemove) {
            Icon(painterResource(R.drawable.ic_close), contentDescription = "Убрать ${ingredient.name}")
        }
    }
}

@Composable
private fun IngredientPickerDialog(
    ingredients: List<Ingredient>,
    onPick: (Ingredient) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ингредиент") },
        text = {
            if (ingredients.isEmpty()) {
                Text("Все ингредиенты склада уже в рецепте. Новые можно добавить на вкладке «Склад»")
            } else {
                Card {
                    LazyColumn(modifier = Modifier.heightIn(max = 360.dp)) {
                        items(ingredients, key = { it.id }) { ingredient ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onPick(ingredient) }
                                    .padding(horizontal = 16.dp, vertical = 12.dp)
                            ) {
                                Text(ingredient.name, style = MaterialTheme.typography.bodyLarge)
                                Text(
                                    "На складе: ${ingredient.format(ingredient.stock)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            HorizontalDivider()
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("Закрыть") } }
    )
}

@Preview(showBackground = true)
@Composable
private fun MenuItemEditPreview() {
    PizzeriaTheme {
        MenuItemEditContent(
            state = MenuItemEditUiState(
                form = MenuItemForm(
                    name = "Карбонара",
                    price = "11",
                    toppings = "бекон, сливки",
                    recipe = listOf(RecipeInput(1, "250"), RecipeInput(3, ""))
                ),
                errors = MenuItemErrors(recipe = mapOf(3 to "Больше 0")),
                ingredients = SampleData.ingredients,
                isLoading = false
            ),
            onBack = {},
            onFormChange = {},
            onAddIngredient = {},
            onAmountChange = { _, _ -> },
            onRemoveIngredient = {},
            onSave = {}
        )
    }
}
