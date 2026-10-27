package com.example.data

import com.example.service.Pizzeria
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class AppContainer {
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val pizzeria = Pizzeria("Roma", SampleData.menu, SampleData.promoCodes)
    val menuRepository = MenuRepository(pizzeria, SampleData.extras)
    val cartRepository = CartRepository()
    val orderRepository = OrderRepository(pizzeria, cartRepository, appScope)
}
