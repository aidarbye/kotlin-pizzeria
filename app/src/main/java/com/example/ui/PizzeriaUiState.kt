package com.example.ui

import com.example.model.Category
import com.example.model.MenuItem
import com.example.model.Order
import com.example.model.OrderLine

data class PizzeriaUiState(
    val pizzeriaName: String = "",
    val query: String = "",
    val category: Category = Category.ALL,
    val vegetarianOnly: Boolean = false,
    val visibleMenu: List<MenuItem> = emptyList(),
    val cart: Map<Int, Int> = emptyMap(),
    val cartLines: List<OrderLine> = emptyList(),
    val orders: List<Order> = emptyList(),
    val revenue: Double = 0.0,
    val popularity: Map<String, Int> = emptyMap()
) {
    val cartCount: Int
        get() = cart.values.sum()

    val cartTotal: Double
        get() = cartLines.sumOf { it.subtotal }

    val activeOrders: Int
        get() = orders.count { it.isActive }
}
