package com.example.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.SampleData
import com.example.model.Category
import com.example.model.Customer
import com.example.model.MenuItem
import com.example.model.Order
import com.example.model.OrderLine
import com.example.model.OrderStatus
import com.example.service.Pizzeria
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PizzeriaViewModel(
    private val pizzeria: Pizzeria = SampleData.pizzeria
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        PizzeriaUiState(
            pizzeriaName = pizzeria.name,
            visibleMenu = pizzeria.menu
        )
    )
    val uiState: StateFlow<PizzeriaUiState> = _uiState.asStateFlow()

    private val orderJobs = mutableMapOf<Int, Job>()
    private var nextOrderId = 1

    init {
        Log.d(TAG, "ViewModel created")
    }

    fun onQueryChange(query: String) = _uiState.update { it.copy(query = query).filtered() }

    fun onCategorySelect(category: Category) = _uiState.update { it.copy(category = category).filtered() }

    fun onVegetarianOnlyChange(enabled: Boolean) =
        _uiState.update { it.copy(vegetarianOnly = enabled).filtered() }

    fun addToCart(item: MenuItem) = _uiState.update { state ->
        state.withCart(state.cart + (item.id to (state.cart[item.id] ?: 0) + 1))
    }

    fun removeFromCart(item: MenuItem) = _uiState.update { state ->
        val quantity = (state.cart[item.id] ?: 0) - 1
        state.withCart(if (quantity > 0) state.cart + (item.id to quantity) else state.cart - item.id)
    }

    fun checkout(name: String, address: String, studentDiscount: Boolean) {
        val lines = _uiState.value.cartLines
        if (lines.isEmpty() || name.isBlank() || address.isBlank()) return

        val draft = Order(nextOrderId++, Customer(name.trim(), address.trim()), lines)
        val order = draft.copy(
            paid = pizzeria.totalWithDiscount(draft) { total -> if (studentDiscount) total * 0.9 else total }
        )
        _uiState.update { it.copy(orders = listOf(order) + it.orders).withCart(emptyMap()).withStats() }

        orderJobs[order.id] = viewModelScope.launch {
            pizzeria.processOrder(order) { status -> updateStatus(order.id, status) }
            orderJobs.remove(order.id)
        }
    }

    fun cancelOrder(orderId: Int) {
        orderJobs.remove(orderId)?.cancel()
        updateStatus(orderId, OrderStatus.Cancelled("клиентом"))
    }

    override fun onCleared() {
        Log.d(TAG, "ViewModel cleared")
    }

    private fun updateStatus(orderId: Int, status: OrderStatus) = _uiState.update { state ->
        state.copy(orders = state.orders.map { if (it.id == orderId) it.copy(status = status) else it })
            .withStats()
    }

    private fun PizzeriaUiState.filtered() =
        copy(visibleMenu = pizzeria.search(query, category, vegetarianOnly))

    private fun PizzeriaUiState.withCart(cart: Map<Int, Int>) = copy(
        cart = cart,
        cartLines = cart.mapNotNull { (id, quantity) -> pizzeria.findItem(id)?.let { OrderLine(it, quantity) } }
    )

    private fun PizzeriaUiState.withStats() = copy(
        revenue = pizzeria.totalRevenue(orders),
        popularity = pizzeria.popularity(orders)
    )

    private companion object {
        const val TAG = "PizzeriaLifecycle"
    }
}
