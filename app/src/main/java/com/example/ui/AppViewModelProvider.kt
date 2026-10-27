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
import com.example.ui.menu.MenuViewModel
import com.example.ui.orders.OrderDetailsViewModel
import com.example.ui.orders.OrdersViewModel

object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer { MainViewModel(container().cartRepository, container().orderRepository) }
        initializer { MenuViewModel(container().menuRepository, container().cartRepository) }
        initializer {
            ItemDetailsViewModel(createSavedStateHandle(), container().menuRepository, container().cartRepository)
        }
        initializer { CartViewModel(container().cartRepository, container().pizzeria) }
        initializer {
            CheckoutViewModel(createSavedStateHandle(), container().cartRepository, container().orderRepository, container().pizzeria)
        }
        initializer { OrdersViewModel(container().orderRepository, container().pizzeria) }
        initializer { OrderDetailsViewModel(createSavedStateHandle(), container().orderRepository) }
    }
}

private fun CreationExtras.container(): AppContainer =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as PizzeriaApplication).container
