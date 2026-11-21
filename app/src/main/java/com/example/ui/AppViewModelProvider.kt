package com.example.ui

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.PizzeriaApplication
import com.example.data.AppContainer
import com.example.ui.cart.CartViewModel
import com.example.ui.checkout.CheckoutViewModel
import com.example.ui.details.ItemDetailsViewModel
import com.example.ui.editor.MenuItemEditViewModel
import com.example.ui.inventory.IngredientDetailsViewModel
import com.example.ui.inventory.IngredientEditViewModel
import com.example.ui.inventory.InventoryViewModel
import com.example.ui.menu.MenuViewModel
import com.example.ui.orders.OrderDetailsViewModel
import com.example.ui.orders.OrdersViewModel

object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            val c = container()
            MainViewModel(c.cartRepository, c.orderRepository, c.inventoryRepository, c.messages)
        }
        initializer {
            val c = container()
            MenuViewModel(c.menuRepository, c.inventoryRepository, c.cartRepository, c.pizzeria, c.messages)
        }
        initializer {
            val c = container()
            ItemDetailsViewModel(createSavedStateHandle(), c.menuRepository, c.inventoryRepository, c.cartRepository, c.messages)
        }
        initializer {
            val c = container()
            MenuItemEditViewModel(createSavedStateHandle(), c.menuRepository, c.inventoryRepository, c.messages)
        }
        initializer {
            val c = container()
            CartViewModel(c.cartRepository, c.menuRepository, c.inventoryRepository, c.pizzeria)
        }
        initializer {
            val c = container()
            CheckoutViewModel(
                createSavedStateHandle(),
                c.cartRepository,
                c.menuRepository,
                c.inventoryRepository,
                c.orderRepository,
                c.pizzeria,
                c.messages
            )
        }
        initializer { OrdersViewModel(container().orderRepository, container().pizzeria) }
        initializer { OrderDetailsViewModel(createSavedStateHandle(), container().orderRepository, container().messages) }
        initializer { InventoryViewModel(container().inventoryRepository, container().menuRepository) }
        initializer {
            IngredientDetailsViewModel(createSavedStateHandle(), container().inventoryRepository, container().messages)
        }
        initializer {
            IngredientEditViewModel(createSavedStateHandle(), container().inventoryRepository, container().messages)
        }
    }
}

private fun CreationExtras.container(): AppContainer =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as PizzeriaApplication).container
