package com.example.model

data class Customer(
    val name: String,
    val address: String
)

data class OrderLine(
    val item: MenuItem,
    val quantity: Int
) {
    val subtotal: Double
        get() = item.price * quantity
}

data class Order(
    val id: Int,
    val customer: Customer,
    val lines: List<OrderLine>,
    val status: OrderStatus = OrderStatus.Received,
    val paid: Double = lines.sumOf { it.subtotal }
) {
    fun total(): Double = lines.sumOf { it.subtotal }

    val isActive: Boolean
        get() = status is OrderStatus.Received || status is OrderStatus.Cooking
}
