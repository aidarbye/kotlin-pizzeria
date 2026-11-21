package com.example.ui.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.CartRepository
import com.example.data.InventoryRepository
import com.example.data.MenuRepository
import com.example.model.DeliveryType
import com.example.model.OrderLine
import com.example.model.PriceQuote
import com.example.model.PromoCode
import com.example.model.Shortage
import com.example.service.Pizzeria
import com.example.service.StockCalculator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CartUiState(
    val lines: List<OrderLine> = emptyList(),
    val promo: PromoCode? = null,
    val promoError: String? = null,
    val quote: PriceQuote = PriceQuote(0.0, 0.0, 0.0),
    val shortages: List<Shortage> = emptyList(),
    val isLoading: Boolean = true
) {
    val count: Int
        get() = lines.sumOf { it.quantity }

    val promoHint: String?
        get() = promo?.takeIf { quote.discount == 0.0 && !it.freeDelivery }?.description
}

class CartViewModel(
    private val cartRepository: CartRepository,
    menuRepository: MenuRepository,
    inventoryRepository: InventoryRepository,
    private val pizzeria: Pizzeria
) : ViewModel() {

    private val promoError = MutableStateFlow<String?>(null)

    val uiState: StateFlow<CartUiState> =
        combine(cartRepository.cart, promoError, inventoryRepository.ingredients, menuRepository.recipes) { cart, error, ingredients, recipes ->
            CartUiState(
                lines = cart.lines,
                promo = cart.promo,
                promoError = error,
                quote = pizzeria.quote(cart.lines, cart.promo, DeliveryType.PICKUP),
                shortages = StockCalculator.shortages(StockCalculator.requirements(cart.lines, recipes), ingredients),
                isLoading = false
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CartUiState())

    fun increase(line: OrderLine) {
        viewModelScope.launch { cartRepository.changeQuantity(line.key, 1) }
    }

    fun decrease(line: OrderLine) {
        viewModelScope.launch { cartRepository.changeQuantity(line.key, -1) }
    }

    fun remove(line: OrderLine) {
        viewModelScope.launch { cartRepository.remove(line.key) }
    }

    fun applyPromo(code: String) {
        val promo = pizzeria.findPromo(code)
        promoError.value = if (promo == null) "Промокод «${code.trim()}» не найден" else null
        if (promo != null) cartRepository.applyPromo(promo)
    }

    fun removePromo() {
        promoError.value = null
        cartRepository.applyPromo(null)
    }
}
