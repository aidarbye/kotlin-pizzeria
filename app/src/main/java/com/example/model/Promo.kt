package com.example.model

class PromoCode(
    val code: String,
    val description: String,
    private val discountRule: (subtotal: Double) -> Double,
    val freeDelivery: Boolean = false
) {
    fun discountFor(subtotal: Double): Double = discountRule(subtotal).coerceIn(0.0, subtotal)
}
