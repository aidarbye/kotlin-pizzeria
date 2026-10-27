package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.SampleData
import com.example.model.Category
import com.example.model.Dessert
import com.example.model.Drink
import com.example.model.MenuItem
import com.example.model.Pizza
import com.example.model.PizzaSize
import com.example.model.formatPrice
import com.example.ui.theme.PizzeriaTheme

@Composable
fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text("Название или начинка") },
        leadingIcon = { Icon(painterResource(R.drawable.ic_search), contentDescription = null) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(painterResource(R.drawable.ic_close), contentDescription = "Очистить")
                }
            }
        },
        singleLine = true,
        shape = MaterialTheme.shapes.large
    )
}

@Composable
fun CategoryFilterRow(
    selected: Category,
    onSelect: (Category) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Category.entries.forEach { category ->
            FilterChip(
                selected = category == selected,
                onClick = { onSelect(category) },
                label = { Text(category.title) }
            )
        }
    }
}

@Composable
fun ToggleChip(
    text: String,
    selected: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = selected,
        onClick = { onToggle(!selected) },
        label = { Text(text) },
        modifier = modifier,
        leadingIcon = if (selected) {
            { Icon(painterResource(R.drawable.ic_check), contentDescription = null, modifier = Modifier.size(18.dp)) }
        } else {
            null
        }
    )
}

@Composable
fun MenuItemImage(item: MenuItem, modifier: Modifier = Modifier, size: Dp = 64.dp) {
    val (icon, background) = when (item) {
        is Pizza -> R.drawable.ic_pizza to MaterialTheme.colorScheme.primaryContainer
        is Drink -> R.drawable.ic_drink to MaterialTheme.colorScheme.secondaryContainer
        is Dessert -> R.drawable.ic_dessert to MaterialTheme.colorScheme.tertiaryContainer
        else -> R.drawable.ic_pizza to MaterialTheme.colorScheme.surfaceContainerHigh
    }
    Box(
        modifier = modifier
            .size(size)
            .background(background, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(icon),
            contentDescription = item.name,
            modifier = Modifier.size(size * 0.56f),
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
        )
    }
}

@Composable
fun FavoriteButton(
    isFavorite: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(onClick = onToggle, modifier = modifier) {
        Icon(
            painter = painterResource(if (isFavorite) R.drawable.ic_favorite else R.drawable.ic_favorite_border),
            contentDescription = if (isFavorite) "Убрать из избранного" else "В избранное",
            tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ItemLabels(item: MenuItem, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val labels = buildList {
        if (item is Pizza && item.isVegetarian) add("вегетарианская")
        if (item is Pizza && item.isSpicy) add("острая")
        if (item is Dessert && item.isSugarFree) add("без сахара")
    }
    if (labels.isEmpty()) return
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        labels.forEach { label ->
            val spicy = label == "острая"
            InfoLabel(
                text = label,
                container = if (spicy) colors.errorContainer else colors.tertiaryContainer,
                content = if (spicy) colors.onErrorContainer else colors.onTertiaryContainer
            )
        }
    }
}

fun MenuItem.displayPrice(): String =
    if (this is Pizza) "от ${(price * PizzaSize.SMALL.priceFactor).formatPrice()}" else price.formatPrice()

@Composable
fun MenuItemCard(
    item: MenuItem,
    quantity: Int,
    isFavorite: Boolean,
    onOpen: () -> Unit,
    onAdd: () -> Unit,
    onRemove: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onOpen,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Row(
            modifier = Modifier.padding(start = 12.dp, top = 4.dp, bottom = 12.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MenuItemImage(item)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f).padding(top = 8.dp)) {
                Text(item.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                ItemLabels(item, Modifier.padding(vertical = 2.dp))
                Text(
                    text = item.details(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = item.displayPrice(),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                FavoriteButton(isFavorite = isFavorite, onToggle = onToggleFavorite)
                if (quantity == 0) {
                    FilledIconButton(onClick = onAdd, modifier = Modifier.padding(end = 8.dp)) {
                        Icon(painterResource(R.drawable.ic_add), contentDescription = "В корзину")
                    }
                } else {
                    QuantityStepper(
                        quantity = quantity,
                        onAdd = onAdd,
                        onRemove = onRemove,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MenuItemCardPreview() {
    PizzeriaTheme {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            MenuItemCard(SampleData.menu[0], 0, true, {}, {}, {}, {})
            MenuItemCard(SampleData.menu[1], 2, false, {}, {}, {}, {})
        }
    }
}
