package com.example.model

sealed class OrderStatus {
    data object Received : OrderStatus()
    data class Cooking(val progress: Int) : OrderStatus()
    data object Delivering : OrderStatus()
    data class Delivered(val minutes: Int) : OrderStatus()
    data class Cancelled(val reason: String) : OrderStatus()
}

fun OrderStatus.toText(): String = when (this) {
    is OrderStatus.Cancelled -> "Отменён: $reason"
    is OrderStatus.Cooking -> "Готовится: $progress%"
    is OrderStatus.Delivered -> "Доставлен за $minutes мин"
    OrderStatus.Delivering -> "Курьер в пути"
    OrderStatus.Received -> "Принят"
}
