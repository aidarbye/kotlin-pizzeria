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

class Order(
    val id: Int,
    val customer: Customer,
    val lines: List<OrderLine>
) {
    var status: OrderStatus = OrderStatus.Received

    fun total(): Double = lines.sumOf { it.subtotal }
}
