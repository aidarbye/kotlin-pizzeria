package com.example.ui.orders

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.data.OrderRepository
import com.example.model.Order
import com.example.ui.common.UserMessages
import com.example.ui.navigation.OrderDetailsRoute
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class OrderDetailsUiState(
    val order: Order? = null,
    val isLoading: Boolean = true
)

class OrderDetailsViewModel(
    savedStateHandle: SavedStateHandle,
    private val orderRepository: OrderRepository,
    private val messages: UserMessages
) : ViewModel() {

    val orderId: Int = savedStateHandle.toRoute<OrderDetailsRoute>().orderId

    val uiState: StateFlow<OrderDetailsUiState> = orderRepository.order(orderId)
        .map { OrderDetailsUiState(order = it, isLoading = false) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), OrderDetailsUiState())

    fun cancel() {
        viewModelScope.launch {
            val cancelled = orderRepository.cancel(orderId)
            messages.show(
                if (cancelled) "Заказ #$orderId отменён, ингредиенты вернулись на склад"
                else "Заказ #$orderId уже нельзя отменить"
            )
        }
    }

    fun repeat(onRepeated: () -> Unit) {
        viewModelScope.launch {
            val result = orderRepository.repeat(orderId)
            val missing = result.unavailable.takeIf { it.isNotEmpty() }?.joinToString(prefix = ". Нет в меню: ")
            messages.show("В корзину добавлено: ${result.added} шт.${missing.orEmpty()}")
            if (result.added > 0) onRepeated()
        }
    }
}
