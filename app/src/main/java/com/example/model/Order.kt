package com.example.model

data class Customer(
    val name: String,
    val phone: String,
    val address: String
)

data class OrderLine(
    val item: MenuItem,
    val quantity: Int,
    val size: PizzaSize? = null,
    val extras: Set<Extra> = emptySet()
) {
    val key: String
        get() = listOf(item.id, size?.name.orEmpty(), extras.map { it.id }.sorted().joinToString(",")).joinToString("|")

    val unitPrice: Double
        get() = item.priceFor(size, extras)

    val subtotal: Double
        get() = unitPrice * quantity

    fun options(): String = listOfNotNull(
        size?.let { "${it.title}, ${it.cm} см" },
        extras.takeIf { it.isNotEmpty() }?.joinToString { "+ ${it.name}" }
    ).joinToString(" · ")
}

enum class DeliveryType(val title: String) {
    DELIVERY("Доставка"),
    PICKUP("Самовывоз")
}

data class PriceQuote(
    val subtotal: Double,
    val discount: Double,
    val deliveryFee: Double
) {
    val total: Double
        get() = subtotal - discount + deliveryFee
}

data class Order(
    val id: Int,
    val customer: Customer,
    val lines: List<OrderLine>,
    val deliveryType: DeliveryType,
    val quote: PriceQuote,
    val promoCode: String? = null,
    val comment: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val status: OrderStatus = OrderStatus.Received
) {
    val itemCount: Int
        get() = lines.sumOf { it.quantity }

    val isActive: Boolean
        get() = status !is OrderStatus.Delivered && status !is OrderStatus.Cancelled

    val canCancel: Boolean
        get() = status is OrderStatus.Received || status is OrderStatus.Cooking
}
