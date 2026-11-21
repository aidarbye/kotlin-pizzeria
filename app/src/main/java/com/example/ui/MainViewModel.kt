package com.example.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.CartRepository
import com.example.data.InventoryRepository
import com.example.data.OrderRepository
import com.example.ui.common.UserMessages
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class BadgeState(
    val cartCount: Int = 0,
    val activeOrders: Int = 0,
    val lowStock: Int = 0
)

class MainViewModel(
    cartRepository: CartRepository,
    orderRepository: OrderRepository,
    inventoryRepository: InventoryRepository,
    userMessages: UserMessages
) : ViewModel() {

    val badges: StateFlow<BadgeState> =
        combine(cartRepository.cart, orderRepository.orders, inventoryRepository.ingredients) { cart, orders, ingredients ->
            BadgeState(
                cartCount = cart.count,
                activeOrders = orders.count { it.isActive },
                lowStock = ingredients.count { it.isLow || it.isOut }
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), BadgeState())

    /** Feedback messages from all screens, shown in the app-wide snackbar. */
    val messages: Flow<String> = userMessages.messages

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
