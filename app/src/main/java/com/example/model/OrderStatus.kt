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

fun OrderStatus.progress(): Float = when (this) {
    is OrderStatus.Cancelled -> 0f
    is OrderStatus.Cooking -> 0.1f + progress / 100f * 0.6f
    is OrderStatus.Delivered -> 1f
    OrderStatus.Delivering -> 0.85f
    OrderStatus.Received -> 0.05f
}
