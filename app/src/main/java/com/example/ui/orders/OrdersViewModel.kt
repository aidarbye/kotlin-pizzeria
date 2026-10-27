package com.example.ui.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.OrderRepository
import com.example.model.Order
import com.example.service.Pizzeria
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class OrdersUiState(
    val active: List<Order> = emptyList(),
    val history: List<Order> = emptyList(),
    val revenue: Double = 0.0,
    val popularity: List<Pair<String, Int>> = emptyList()
) {
    val isEmpty: Boolean
        get() = active.isEmpty() && history.isEmpty()
}

class OrdersViewModel(
    orderRepository: OrderRepository,
    private val pizzeria: Pizzeria
) : ViewModel() {

    val uiState: StateFlow<OrdersUiState> = orderRepository.orders
        .map { orders ->
            val (active, history) = orders.partition { it.isActive }
            OrdersUiState(
                active = active,
                history = history,
                revenue = pizzeria.totalRevenue(orders),
                popularity = pizzeria.popularity(orders).toList().sortedByDescending { it.second }.take(3)
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), OrdersUiState())
}
