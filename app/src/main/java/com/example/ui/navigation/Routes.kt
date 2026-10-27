package com.example.ui.navigation

import androidx.annotation.DrawableRes
import com.example.R
import kotlinx.serialization.Serializable

@Serializable
data object MenuRoute

@Serializable
data object CartRoute

@Serializable
data object OrdersRoute

@Serializable
data class ItemDetailsRoute(val itemId: Int)

@Serializable
data object CheckoutRoute

@Serializable
data class OrderDetailsRoute(val orderId: Int)

enum class TopLevelTab(val route: Any, val title: String, @DrawableRes val icon: Int) {
    MENU(MenuRoute, "Меню", R.drawable.ic_pizza),
    CART(CartRoute, "Корзина", R.drawable.ic_cart),
    ORDERS(OrdersRoute, "Заказы", R.drawable.ic_orders)
}
