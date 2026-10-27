package com.example.ui.checkout

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.CartRepository
import com.example.data.OrderRepository
import com.example.model.DeliveryType
import com.example.model.OrderLine
import com.example.model.PriceQuote
import com.example.service.Pizzeria
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class CheckoutUiState(
    val lines: List<OrderLine> = emptyList(),
    val deliveryType: DeliveryType = DeliveryType.DELIVERY,
    val promoCode: String? = null,
    val quote: PriceQuote = PriceQuote(0.0, 0.0, 0.0)
)

class CheckoutViewModel(
    private val savedStateHandle: SavedStateHandle,
    cartRepository: CartRepository,
    private val orderRepository: OrderRepository,
    private val pizzeria: Pizzeria
) : ViewModel() {

    private val deliveryType = savedStateHandle.getStateFlow(KEY_DELIVERY, DeliveryType.DELIVERY)

    val uiState: StateFlow<CheckoutUiState> =
        combine(cartRepository.cart, deliveryType) { cart, type ->
            CheckoutUiState(
                lines = cart.lines,
                deliveryType = type,
                promoCode = cart.promo?.code,
                quote = pizzeria.quote(cart.lines, cart.promo, type)
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CheckoutUiState())

    fun selectDeliveryType(type: DeliveryType) {
        savedStateHandle[KEY_DELIVERY] = type
    }

    fun placeOrder(form: CheckoutForm): Int? {
        val type = deliveryType.value
        if (!form.errors(type).isValid) return null
        return orderRepository.placeOrder(form.toCustomer(type), type, form.comment.trim())?.id
    }

    private companion object {
        const val KEY_DELIVERY = "deliveryType"
    }
}
