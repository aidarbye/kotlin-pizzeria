package com.example.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.CartRepository
import com.example.data.OrderRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class BadgeState(
    val cartCount: Int = 0,
    val activeOrders: Int = 0
)

class MainViewModel(
    cartRepository: CartRepository,
    orderRepository: OrderRepository
) : ViewModel() {

    val badges: StateFlow<BadgeState> =
        combine(cartRepository.cart, orderRepository.orders) { cart, orders ->
            BadgeState(cartCount = cart.count, activeOrders = orders.count { it.isActive })
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), BadgeState())

    init {
        Log.d(TAG, "MainViewModel created")
    }

    override fun onCleared() {
        Log.d(TAG, "MainViewModel cleared")
    }

    private companion object {
        const val TAG = "PizzeriaLifecycle"
    }
}
