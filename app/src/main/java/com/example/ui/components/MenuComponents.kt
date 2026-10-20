package com.example.ui.components

import androidx.compose.animation.animateContentSize
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
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.SampleData
import com.example.model.Category
import com.example.model.Dessert
import com.example.model.Drink
import com.example.model.MenuItem
import com.example.model.Pizza
import com.example.model.formatPrice
import com.example.model.isVegetarian
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
fun LabeledCheckbox(
    text: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .toggleable(value = checked, role = Role.Checkbox, onValueChange = onCheckedChange)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = checked, onCheckedChange = null)
        Spacer(Modifier.width(8.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun MenuItemImage(item: MenuItem, modifier: Modifier = Modifier) {
    val (icon, background) = when (item) {
        is Pizza -> R.drawable.ic_pizza to MaterialTheme.colorScheme.primaryContainer
        is Drink -> R.drawable.ic_drink to MaterialTheme.colorScheme.secondaryContainer
        is Dessert -> R.drawable.ic_dessert to MaterialTheme.colorScheme.tertiaryContainer
        else -> R.drawable.ic_pizza to MaterialTheme.colorScheme.surfaceContainerHigh
    }
    Box(
        modifier = modifier
            .size(64.dp)
            .background(background, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(icon),
            contentDescription = item.name,
            modifier = Modifier.size(36.dp),
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
        )
    }
}

@Composable
fun QuantityStepper(
    quantity: Int,
    onAdd: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        FilledTonalIconButton(onClick = onRemove, modifier = Modifier.size(36.dp)) {
            Icon(painterResource(R.drawable.ic_remove), contentDescription = "Убрать")
        }
        Text(
            text = quantity.toString(),
            modifier = Modifier.padding(horizontal = 12.dp),
            style = MaterialTheme.typography.titleMedium
        )
        FilledTonalIconButton(onClick = onAdd, modifier = Modifier.size(36.dp)) {
            Icon(painterResource(R.drawable.ic_add), contentDescription = "Добавить")
        }
    }
}

@Composable
fun VegetarianLabel(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.tertiaryContainer
    ) {
        Text(
            text = "вегетарианская",
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onTertiaryContainer
        )
    }
}

@Composable
fun MenuItemCard(
    item: MenuItem,
    quantity: Int,
    onAdd: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        onClick = { expanded = !expanded },
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .animateContentSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MenuItemImage(item)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(item.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                if (item is Pizza && item.isVegetarian) {
                    VegetarianLabel(Modifier.padding(vertical = 2.dp))
                }
                Text(
                    text = item.details(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = if (expanded) Int.MAX_VALUE else 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = item.price.formatPrice(),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            Spacer(Modifier.width(8.dp))
            if (quantity == 0) {
                Button(onClick = onAdd) { Text("В корзину") }
            } else {
                QuantityStepper(quantity = quantity, onAdd = onAdd, onRemove = onRemove)
            }
        }
    }
}

@Composable
fun CartBar(
    count: Int,
    total: Double,
    onCheckout: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("В корзине: $count шт.", style = MaterialTheme.typography.labelLarge)
                Text(total.formatPrice(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
            Button(onClick = onCheckout) { Text("Оформить") }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MenuItemCardPreview() {
    PizzeriaTheme {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            MenuItemCard(SampleData.menu[0], quantity = 0, onAdd = {}, onRemove = {})
            MenuItemCard(SampleData.menu[6], quantity = 2, onAdd = {}, onRemove = {})
        }
    }
}
