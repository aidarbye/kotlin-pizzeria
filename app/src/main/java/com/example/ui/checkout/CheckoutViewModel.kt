package com.example.ui.checkout

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.CartRepository
import com.example.data.InventoryRepository
import com.example.data.MenuRepository
import com.example.data.OrderRepository
import com.example.data.PlaceOrderResult
import com.example.model.DeliveryType
import com.example.model.OrderLine
import com.example.model.PriceQuote
import com.example.model.Shortage
import com.example.service.Pizzeria
import com.example.service.StockCalculator
import com.example.ui.common.UserMessages
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CheckoutUiState(
    val lines: List<OrderLine> = emptyList(),
    val deliveryType: DeliveryType = DeliveryType.DELIVERY,
    val promoCode: String? = null,
    val quote: PriceQuote = PriceQuote(0.0, 0.0, 0.0),
    val shortages: List<Shortage> = emptyList(),
    val isPlacing: Boolean = false,
    val isLoading: Boolean = true
)

class CheckoutViewModel(
    private val savedStateHandle: SavedStateHandle,
    cartRepository: CartRepository,
    menuRepository: MenuRepository,
    inventoryRepository: InventoryRepository,
    private val orderRepository: OrderRepository,
    private val pizzeria: Pizzeria,
    private val messages: UserMessages
) : ViewModel() {

    private val deliveryType = savedStateHandle.getStateFlow(KEY_DELIVERY, DeliveryType.DELIVERY)
    private val isPlacing = MutableStateFlow(false)
    private val stock = combine(inventoryRepository.ingredients, menuRepository.recipes, ::Pair)

    val uiState: StateFlow<CheckoutUiState> =
        combine(cartRepository.cart, deliveryType, stock, isPlacing) { cart, type, (ingredients, recipes), placing ->
            CheckoutUiState(
                lines = cart.lines,
                deliveryType = type,
                promoCode = cart.promo?.code,
                quote = pizzeria.quote(cart.lines, cart.promo, type),
                shortages = StockCalculator.shortages(StockCalculator.requirements(cart.lines, recipes), ingredients),
                isPlacing = placing,
                isLoading = false
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CheckoutUiState())

    fun selectDeliveryType(type: DeliveryType) {
        savedStateHandle[KEY_DELIVERY] = type
    }

    fun placeOrder(form: CheckoutForm, onPlaced: (Int) -> Unit) {
        val type = deliveryType.value
        if (!form.errors(type).isValid || isPlacing.value) return
        isPlacing.value = true
        viewModelScope.launch {
            when (val result = orderRepository.placeOrder(form.toCustomer(type), type, form.comment.trim())) {
                is PlaceOrderResult.Success -> {
                    messages.show("Заказ #${result.orderId} принят, ингредиенты списаны со склада")
                    onPlaced(result.orderId)
                }
                is PlaceOrderResult.NotEnoughStock ->
                    messages.show("Не хватает: ${result.shortages.joinToString { it.ingredient.name }}")
                PlaceOrderResult.EmptyCart -> messages.show("Корзина пуста")
            }
            isPlacing.value = false
        }
    }

    private companion object {
        const val KEY_DELIVERY = "deliveryType"
    }
}
