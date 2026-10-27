package com.example.model

data class Cart(
    val lines: List<OrderLine> = emptyList(),
    val promo: PromoCode? = null
) {
    val count: Int
        get() = lines.sumOf { it.quantity }

    fun quantityOf(itemId: Int): Int = lines.filter { it.item.id == itemId }.sumOf { it.quantity }
}
