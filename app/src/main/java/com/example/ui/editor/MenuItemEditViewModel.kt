package com.example.ui.editor

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.data.InventoryRepository
import com.example.data.MenuRepository
import com.example.model.Ingredient
import com.example.ui.common.UserMessages
import com.example.ui.navigation.MenuItemEditRoute
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

data class MenuItemEditUiState(
    val isNew: Boolean = true,
    val form: MenuItemForm = MenuItemForm(),
    /** Errors are shown only after the first attempt to save. */
    val errors: MenuItemErrors = MenuItemErrors(),
    val ingredients: List<Ingredient> = emptyList(),
    val isLoading: Boolean = true,
    val notFound: Boolean = false,
    val isSaving: Boolean = false
)

private data class EditorState(
    val form: MenuItemForm = MenuItemForm(),
    val submitted: Boolean = false,
    val loaded: Boolean = false,
    val notFound: Boolean = false,
    val saving: Boolean = false
)

class MenuItemEditViewModel(
    savedStateHandle: SavedStateHandle,
    private val menuRepository: MenuRepository,
    inventoryRepository: InventoryRepository,
    private val messages: UserMessages
) : ViewModel() {

    private val itemId: Int = savedStateHandle.toRoute<MenuItemEditRoute>().itemId
    private val isNew = itemId == NEW_ITEM_ID

    private val editor = MutableStateFlow(EditorState(loaded = isNew))

    /** Lower-case names of the other dishes, to keep names unique. */
    private val otherNames = menuRepository.menu.map { menu ->
        menu.filter { it.id != itemId }.map { it.name.trim().lowercase() }.toSet()
    }

    val uiState: StateFlow<MenuItemEditUiState> =
        combine(editor, inventoryRepository.ingredients, otherNames) { editor, ingredients, names ->
            MenuItemEditUiState(
                isNew = isNew,
                form = editor.form,
                errors = if (editor.submitted) editor.form.validate(names) else MenuItemErrors(),
                ingredients = ingredients,
                isLoading = !editor.loaded,
                notFound = editor.notFound,
                isSaving = editor.saving
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MenuItemEditUiState(isNew = isNew))

    init {
        if (!isNew) {
            viewModelScope.launch {
                val item = menuRepository.getItem(itemId)
                val recipe = menuRepository.getRecipe(itemId)
                editor.update {
                    if (item == null) it.copy(loaded = true, notFound = true)
                    else it.copy(form = MenuItemForm.from(item, recipe), loaded = true)
                }
            }
        }
    }

    fun onFormChange(form: MenuItemForm) = editor.update { it.copy(form = form) }

    fun addIngredient(ingredient: Ingredient) = editor.update { state ->
        if (state.form.recipe.any { it.ingredientId == ingredient.id }) state
        else state.copy(form = state.form.copy(recipe = state.form.recipe + RecipeInput(ingredient.id, "")))
    }

    fun changeAmount(ingredientId: Int, amount: String) = editor.update { state ->
        val recipe = state.form.recipe.map { if (it.ingredientId == ingredientId) it.copy(amount = amount) else it }
        state.copy(form = state.form.copy(recipe = recipe))
    }

    fun removeIngredient(ingredientId: Int) = editor.update { state ->
        state.copy(form = state.form.copy(recipe = state.form.recipe.filterNot { it.ingredientId == ingredientId }))
    }

    fun save(onSaved: (Int) -> Unit) {
        val state = editor.value
        if (state.saving || !state.loaded || state.notFound) return
        editor.update { it.copy(submitted = true) }
        viewModelScope.launch {
            val form = editor.value.form
            if (!form.validate(otherNames.first()).isValid) {
                messages.show("Проверьте поля формы")
                return@launch
            }
            editor.update { it.copy(saving = true) }
            val savedId = menuRepository.save(form.toMenuItem(itemId), form.toRecipe(itemId))
            messages.show(if (isNew) "«${form.name.trim()}» добавлено в меню" else "Изменения сохранены")
            onSaved(savedId)
        }
    }
}
