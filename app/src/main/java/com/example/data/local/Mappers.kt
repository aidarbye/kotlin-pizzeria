package com.example.data.local

import com.example.model.Customer
import com.example.model.Dessert
import com.example.model.DishType
import com.example.model.DishUsage
import com.example.model.Drink
import com.example.model.Extra
import com.example.model.Ingredient
import com.example.model.MenuItem
import com.example.model.Order
import com.example.model.OrderLine
import com.example.model.OrderStatus
import com.example.model.Pizza
import com.example.model.PriceQuote
import com.example.model.RecipeEntry
import com.example.model.StockTransaction
import com.example.model.dishType

fun MenuItemEntity.toModel(): MenuItem = when (type) {
    DishType.PIZZA -> Pizza(id, name, price, description, toppings.splitToSet(), isVegetarian, isSpicy)
    DishType.DRINK -> Drink(id, name, price, description, volumeMl)
    DishType.DESSERT -> Dessert(id, name, price, description, isSugarFree)
}

fun MenuItem.toEntity(isFavorite: Boolean = false): MenuItemEntity = MenuItemEntity(
    id = id,
    type = dishType,
    name = name,
    price = price,
    description = description,
    toppings = (this as? Pizza)?.toppings?.joinToString(", ").orEmpty(),
    isVegetarian = (this as? Pizza)?.isVegetarian ?: false,
    isSpicy = (this as? Pizza)?.isSpicy ?: false,
    volumeMl = (this as? Drink)?.volumeMl ?: 0,
    isSugarFree = (this as? Dessert)?.isSugarFree ?: false,
    isFavorite = isFavorite
)

fun IngredientEntity.toModel() = Ingredient(id, name, unit, stock, lowThreshold)

fun Ingredient.toEntity() = IngredientEntity(id, name, unit, stock, lowThreshold)

fun RecipeItemEntity.toModel() = RecipeEntry(menuItemId, ingredientId, amount)

fun RecipeEntry.toEntity() = RecipeItemEntity(menuItemId, ingredientId, amount)

fun StockTransactionEntity.toModel() = StockTransaction(id, ingredientId, type, delta, orderId, note, createdAt)

fun DishUsageRow.toModel() = DishUsage(menuItemId, name, type, amount)

fun CartLineEntity.toModel(item: MenuItem, extras: List<Extra>) = OrderLine(
    item = item,
    quantity = quantity,
    size = size,
    extras = extras.filter { it.id in extraIds.splitToIds() }.toSet()
)

fun OrderLine.toCartEntity() = CartLineEntity(
    key = key,
    menuItemId = item.id,
    quantity = quantity,
    size = size,
    extraIds = extras.joinToString(",") { it.id.toString() }
)

fun OrderLine.toOrderLineEntity(orderId: Int) = OrderLineEntity(
    orderId = orderId,
    menuItemId = item.id,
    itemType = item.dishType,
    itemName = item.name,
    basePrice = item.price,
    quantity = quantity,
    size = size,
    extraIds = extras.joinToString(",") { it.id.toString() }
)

fun OrderWithLines.toModel(extras: List<Extra>): Order = Order(
    id = order.id,
    customer = Customer(order.customerName, order.phone, order.address),
    lines = lines.map { line ->
        OrderLine(
            item = snapshotItem(line),
            quantity = line.quantity,
            size = line.size,
            extras = extras.filter { it.id in line.extraIds.splitToIds() }.toSet()
        )
    },
    deliveryType = order.deliveryType,
    quote = PriceQuote(order.subtotal, order.discount, order.deliveryFee),
    promoCode = order.promoCode,
    comment = order.comment,
    createdAt = order.createdAt,
    status = order.toStatus()
)

fun OrderStatus.toColumns(): Triple<StatusType, Int, String?> = when (this) {
    OrderStatus.Received -> Triple(StatusType.RECEIVED, 0, null)
    is OrderStatus.Cooking -> Triple(StatusType.COOKING, progress, null)
    OrderStatus.Delivering -> Triple(StatusType.DELIVERING, 0, null)
    is OrderStatus.Delivered -> Triple(StatusType.DELIVERED, minutes, null)
    is OrderStatus.Cancelled -> Triple(StatusType.CANCELLED, 0, reason)
}

private fun OrderEntity.toStatus(): OrderStatus = when (statusType) {
    StatusType.RECEIVED -> OrderStatus.Received
    StatusType.COOKING -> OrderStatus.Cooking(statusValue)
    StatusType.DELIVERING -> OrderStatus.Delivering
    StatusType.DELIVERED -> OrderStatus.Delivered(statusValue)
    StatusType.CANCELLED -> OrderStatus.Cancelled(cancelReason.orEmpty())
}

/** The dish as it was when the order was placed. */
private fun snapshotItem(line: OrderLineEntity): MenuItem = when (line.itemType) {
    DishType.PIZZA -> Pizza(line.menuItemId, line.itemName, line.basePrice, "", emptySet(), isVegetarian = false)
    DishType.DRINK -> Drink(line.menuItemId, line.itemName, line.basePrice, "", 0)
    DishType.DESSERT -> Dessert(line.menuItemId, line.itemName, line.basePrice, "", isSugarFree = false)
}

private fun String.splitToSet(): Set<String> = split(",").map { it.trim() }.filter { it.isNotEmpty() }.toSet()

private fun String.splitToIds(): Set<Int> = split(",").mapNotNull { it.trim().toIntOrNull() }.toSet()
