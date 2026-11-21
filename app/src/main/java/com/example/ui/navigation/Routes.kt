package com.example.ui.navigation

import androidx.annotation.DrawableRes
import com.example.R
import kotlinx.serialization.Serializable

/** Id used in edit routes when a new object is created. */
const val NEW_ITEM_ID = 0

@Serializable
data object MenuRoute

@Serializable
data object CartRoute

@Serializable
data object OrdersRoute

@Serializable
data object InventoryRoute

@Serializable
data class ItemDetailsRoute(val itemId: Int)

@Serializable
data class MenuItemEditRoute(val itemId: Int = NEW_ITEM_ID)

@Serializable
data object CheckoutRoute

@Serializable
data class OrderDetailsRoute(val orderId: Int)

@Serializable
data class IngredientDetailsRoute(val ingredientId: Int)

@Serializable
data class IngredientEditRoute(val ingredientId: Int = NEW_ITEM_ID)

enum class TopLevelTab(val route: Any, val title: String, @DrawableRes val icon: Int) {
    MENU(MenuRoute, "Меню", R.drawable.ic_pizza),
    CART(CartRoute, "Корзина", R.drawable.ic_cart),
    ORDERS(OrdersRoute, "Заказы", R.drawable.ic_orders),
    INVENTORY(InventoryRoute, "Склад", R.drawable.ic_inventory)
}
