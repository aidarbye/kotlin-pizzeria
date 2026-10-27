package com.example.ui.orders

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.data.OrderRepository
import com.example.model.Order
import com.example.ui.navigation.OrderDetailsRoute
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class OrderDetailsViewModel(
    savedStateHandle: SavedStateHandle,
    private val orderRepository: OrderRepository
) : ViewModel() {

    val orderId: Int = savedStateHandle.toRoute<OrderDetailsRoute>().orderId

    val order: StateFlow<Order?> = orderRepository.orders
        .map { orders -> orders.find { it.id == orderId } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = orderRepository.orders.value.find { it.id == orderId }
        )

    fun cancel() = orderRepository.cancel(orderId)

    fun repeat() = orderRepository.repeat(orderId)
}
