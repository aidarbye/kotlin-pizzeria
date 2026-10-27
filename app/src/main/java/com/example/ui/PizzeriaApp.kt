package com.example.ui

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.cart.CartScreen
import com.example.ui.checkout.CheckoutScreen
import com.example.ui.details.ItemDetailsScreen
import com.example.ui.menu.MenuScreen
import com.example.ui.navigation.CartRoute
import com.example.ui.navigation.CheckoutRoute
import com.example.ui.navigation.ItemDetailsRoute
import com.example.ui.navigation.MenuRoute
import com.example.ui.navigation.OrderDetailsRoute
import com.example.ui.navigation.OrdersRoute
import com.example.ui.navigation.TopLevelTab
import com.example.ui.orders.OrderDetailsScreen
import com.example.ui.orders.OrdersScreen

@Composable
fun PizzeriaApp(
    navController: NavHostController = rememberNavController(),
    mainViewModel: MainViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val badges by mainViewModel.badges.collectAsStateWithLifecycle()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val destination = backStackEntry?.destination
    val showBottomBar = TopLevelTab.entries.any { destination.isOnTab(it) }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    TopLevelTab.entries.forEach { tab ->
                        NavigationBarItem(
                            selected = destination.isOnTab(tab),
                            onClick = { navController.navigateToTab(tab) },
                            icon = {
                                BadgedBox(badge = {
                                    val count = when (tab) {
                                        TopLevelTab.CART -> badges.cartCount
                                        TopLevelTab.ORDERS -> badges.activeOrders
                                        TopLevelTab.MENU -> 0
                                    }
                                    if (count > 0) Badge { Text(count.toString()) }
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
        NavHost(
            navController = navController,
            startDestination = MenuRoute,
            modifier = Modifier
                .padding(padding)
                .consumeWindowInsets(padding)
        ) {
            composable<MenuRoute> {
                MenuScreen(
                    onOpenItem = { id -> navController.navigate(ItemDetailsRoute(id)) },
                    onOpenCart = { navController.navigateToTab(TopLevelTab.CART) }
                )
            }
            composable<ItemDetailsRoute> {
                ItemDetailsScreen(onBack = { navController.popBackStack() })
            }
            composable<CartRoute> {
                CartScreen(
                    onCheckout = { navController.navigate(CheckoutRoute) },
                    onOpenMenu = { navController.navigateToTab(TopLevelTab.MENU) }
                )
            }
            composable<CheckoutRoute> {
                CheckoutScreen(
                    onBack = { navController.popBackStack() },
                    onOrderPlaced = { orderId ->
                        navController.navigate(OrderDetailsRoute(orderId)) {
                            popUpTo(CartRoute) { inclusive = true }
                        }
                    }
                )
            }
            composable<OrdersRoute> {
                OrdersScreen(
                    onOpenOrder = { id -> navController.navigate(OrderDetailsRoute(id)) },
                    onOpenMenu = { navController.navigateToTab(TopLevelTab.MENU) }
                )
            }
            composable<OrderDetailsRoute> {
                OrderDetailsScreen(
                    onBack = { navController.popBackStack() },
                    onRepeated = { navController.navigateToTab(TopLevelTab.CART) }
                )
            }
        }
    }
}

private fun NavDestination?.isOnTab(tab: TopLevelTab): Boolean =
    this?.hierarchy?.any { it.hasRoute(tab.route::class) } == true

private fun NavHostController.navigateToTab(tab: TopLevelTab) {
    navigate(tab.route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
