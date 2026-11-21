package com.example.ui.inventory

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.data.InventoryRepository
import com.example.ui.common.UserMessages
import com.example.ui.navigation.IngredientEditRoute
import com.example.ui.navigation.NEW_ITEM_ID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class IngredientEditUiState(
    val isNew: Boolean = true,
    val form: IngredientForm = IngredientForm(),
    val errors: IngredientErrors = IngredientErrors(),
    val isLoading: Boolean = true,
    val notFound: Boolean = false,
    val isSaving: Boolean = false
)

private data class IngredientEditorState(
    val form: IngredientForm = IngredientForm(),
    val submitted: Boolean = false,
    val loaded: Boolean = false,
    val notFound: Boolean = false,
    val saving: Boolean = false
)

class IngredientEditViewModel(
    savedStateHandle: SavedStateHandle,
    private val inventoryRepository: InventoryRepository,
    private val messages: UserMessages
) : ViewModel() {

    private val ingredientId = savedStateHandle.toRoute<IngredientEditRoute>().ingredientId
    private val isNew = ingredientId == NEW_ITEM_ID

    private val editor = MutableStateFlow(IngredientEditorState(loaded = isNew))

    private val otherNames = inventoryRepository.ingredients.map { list ->
        list.filter { it.id != ingredientId }.map { it.name.trim().lowercase() }.toSet()
    }

    val uiState: StateFlow<IngredientEditUiState> =
        combine(editor, otherNames) { editor, names ->
            IngredientEditUiState(
                isNew = isNew,
                form = editor.form,
                errors = if (editor.submitted) editor.form.validate(isNew, names) else IngredientErrors(),
                isLoading = !editor.loaded,
                notFound = editor.notFound,
                isSaving = editor.saving
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), IngredientEditUiState(isNew = isNew))

    init {
        if (!isNew) {
            viewModelScope.launch {
                val ingredient = inventoryRepository.getIngredient(ingredientId)
                editor.update {
                    if (ingredient == null) it.copy(loaded = true, notFound = true)
                    else it.copy(form = IngredientForm.from(ingredient), loaded = true)
                }
            }
        }
    }

    fun onFormChange(form: IngredientForm) = editor.update { it.copy(form = form) }

    fun save(onSaved: (Int) -> Unit) {
        val state = editor.value
        if (state.saving || !state.loaded || state.notFound) return
        editor.update { it.copy(submitted = true) }
        viewModelScope.launch {
            val form = editor.value.form
            if (!form.validate(isNew, otherNames.first()).isValid) {
                messages.show("Проверьте поля формы")
                return@launch
            }
            editor.update { it.copy(saving = true) }
            val ingredient = form.toIngredient(ingredientId)
            val id = if (isNew) {
                inventoryRepository.create(ingredient)
            } else {
                inventoryRepository.updateInfo(ingredient)
                ingredientId
            }
            messages.show(if (isNew) "Ингредиент «${ingredient.name}» добавлен" else "Изменения сохранены")
            onSaved(id)
        }
    }
}
