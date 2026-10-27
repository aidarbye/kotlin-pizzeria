package com.example.ui.checkout

import com.example.model.Customer
import com.example.model.DeliveryType

data class CheckoutForm(
    val name: String = "",
    val phone: String = "",
    val address: String = "",
    val comment: String = ""
) {
    fun errors(deliveryType: DeliveryType): CheckoutErrors = CheckoutErrors(
        name = "Введите имя".takeIf { name.isBlank() },
        phone = "Номер должен содержать 10-12 цифр".takeIf { phone.count(Char::isDigit) !in 10..12 },
        address = "Укажите адрес доставки".takeIf { deliveryType == DeliveryType.DELIVERY && address.isBlank() }
    )

    fun toCustomer(deliveryType: DeliveryType) = Customer(
        name = name.trim(),
        phone = phone.trim(),
        address = if (deliveryType == DeliveryType.DELIVERY) address.trim() else PICKUP_ADDRESS
    )

    companion object {
        const val PICKUP_ADDRESS = "ул. Абая, 10"
    }
}

data class CheckoutErrors(
    val name: String? = null,
    val phone: String? = null,
    val address: String? = null
) {
    val isValid: Boolean
        get() = listOfNotNull(name, phone, address).isEmpty()
}
