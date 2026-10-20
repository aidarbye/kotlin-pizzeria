package com.example.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.R
import com.example.ui.components.CartBar
import com.example.ui.components.CheckoutDialog
import com.example.ui.screens.MenuScreen
import com.example.ui.screens.OrdersScreen

enum class AppTab(val title: String, @DrawableRes val icon: Int) {
    MENU("Меню", R.drawable.ic_pizza),
    ORDERS("Заказы", R.drawable.ic_orders)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PizzeriaApp(viewModel: PizzeriaViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    var selectedTab by rememberSaveable { mutableStateOf(AppTab.MENU) }
    var showCheckout by rememberSaveable { mutableStateOf(false) }
    val tabStateHolder = rememberSaveableStateHolder()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text("Пиццерия ${state.pizzeriaName}") })
        },
        bottomBar = {
            Column {
                if (selectedTab == AppTab.MENU && state.cartCount > 0) {
                    CartBar(
                        count = state.cartCount,
                        total = state.cartTotal,
                        onCheckout = { showCheckout = true }
                    )
                }
                NavigationBar {
                    AppTab.entries.forEach { tab ->
                        NavigationBarItem(
                            selected = tab == selectedTab,
                            onClick = { selectedTab = tab },
                            icon = {
                                BadgedBox(badge = {
                                    if (tab == AppTab.ORDERS && state.activeOrders > 0) {
                                        Badge { Text(state.activeOrders.toString()) }
                                    }
                                }) {
                                    Icon(painterResource(tab.icon), contentDescription = null)
                                }
                            },
                            label = { Text(tab.title) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            tabStateHolder.SaveableStateProvider(selectedTab.name) {
                when (selectedTab) {
                    AppTab.MENU -> MenuScreen(
                        state = state,
                        onQueryChange = viewModel::onQueryChange,
                        onCategorySelect = viewModel::onCategorySelect,
                        onVegetarianOnlyChange = viewModel::onVegetarianOnlyChange,
                        onAdd = viewModel::addToCart,
                        onRemove = viewModel::removeFromCart
                    )

                    AppTab.ORDERS -> OrdersScreen(
                        orders = state.orders,
                        revenue = state.revenue,
                        popularity = state.popularity,
                        onCancel = viewModel::cancelOrder
                    )
                }
            }
        }
    }

    if (showCheckout) {
        CheckoutDialog(
            total = state.cartTotal,
            onDismiss = { showCheckout = false },
            onConfirm = { name, address, discount ->
                viewModel.checkout(name, address, discount)
                showCheckout = false
                selectedTab = AppTab.ORDERS
            }
        )
    }
}
