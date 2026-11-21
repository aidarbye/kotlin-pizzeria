package com.example.data

import android.content.Context
import com.example.data.local.DatabaseSeeder
import com.example.data.local.PizzeriaDatabase
import com.example.service.Pizzeria
import com.example.ui.common.UserMessages
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/** Manual dependency injection: one instance of each data source and repository for the whole app. */
class AppContainer(context: Context) {
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val database: PizzeriaDatabase by lazy {
        PizzeriaDatabase.build(context) { appScope.launch { DatabaseSeeder(database).seed() } }
    }

    val pizzeria = Pizzeria("Roma", SampleData.promoCodes)
    val messages = UserMessages()

    val menuRepository by lazy { MenuRepository(database.menuDao(), SampleData.extras, appScope) }
    val inventoryRepository by lazy { InventoryRepository(database, database.inventoryDao(), appScope) }
    val cartRepository by lazy { CartRepository(database.cartDao(), database.menuDao(), SampleData.extras, appScope) }
    val orderRepository by lazy {
        OrderRepository(
            database = database,
            orderDao = database.orderDao(),
            menuDao = database.menuDao(),
            inventoryDao = database.inventoryDao(),
            cartRepository = cartRepository,
            pizzeria = pizzeria,
            extras = SampleData.extras,
            scope = appScope
        )
    }
}
