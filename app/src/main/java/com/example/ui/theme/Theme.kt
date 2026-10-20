package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LightColors = lightColorScheme(
    primary = Color(0xFFC62828),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFFFDAD5),
    onPrimaryContainer = Color(0xFF410001),
    secondary = Color(0xFF775651),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFFFDAD5),
    onSecondaryContainer = Color(0xFF2C1512),
    tertiary = Color(0xFF3B6939),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFBCF0B4),
    onTertiaryContainer = Color(0xFF002204),
    background = Color(0xFFFFF8F6),
    surface = Color(0xFFFFF8F6),
    surfaceContainerLow = Color(0xFFFFF0EE),
    surfaceContainer = Color(0xFFFCEAE7),
    surfaceContainerHigh = Color(0xFFF6E4E1),
    surfaceContainerHighest = Color(0xFFF0DEDB),
    surfaceVariant = Color(0xFFF5DDDA)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFFFB4A9),
    onPrimary = Color(0xFF690002),
    primaryContainer = Color(0xFF930006),
    onPrimaryContainer = Color(0xFFFFDAD5),
    secondary = Color(0xFFE7BDB6),
    onSecondary = Color(0xFF442925),
    secondaryContainer = Color(0xFF5D3F3B),
    onSecondaryContainer = Color(0xFFFFDAD5),
    tertiary = Color(0xFFA1D39A),
    onTertiary = Color(0xFF0A390F),
    tertiaryContainer = Color(0xFF235024),
    onTertiaryContainer = Color(0xFFBCF0B4),
    background = Color(0xFF1A1110),
    surface = Color(0xFF1A1110),
    surfaceContainerLow = Color(0xFF231918),
    surfaceContainer = Color(0xFF271D1C),
    surfaceContainerHigh = Color(0xFF322826),
    surfaceContainerHighest = Color(0xFF3D3231),
    surfaceVariant = Color(0xFF534341)
)

private val AppShapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp)
)

@Composable
fun PizzeriaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        shapes = AppShapes,
        content = content
    )
}
