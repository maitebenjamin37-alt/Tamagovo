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

private val LightColorScheme = lightColorScheme(
    primary = AmberGoldPrimary,
    onPrimary = AmberGoldOnPrimary,
    primaryContainer = AmberGoldContainer,
    onPrimaryContainer = AmberGoldOnContainer,
    secondary = MintSecondary,
    onSecondary = MintOnSecondary,
    secondaryContainer = MintContainer,
    onSecondaryContainer = MintOnContainer,
    tertiary = BerryTertiary,
    onTertiary = BerryOnTertiary,
    tertiaryContainer = BerryContainer,
    onTertiaryContainer = BerryOnContainer,
    background = CreamBackground,
    onBackground = DarkTextPrimary,
    surface = CreamSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = WarmSurfaceVariant,
    onSurfaceVariant = DarkTextSecondary
)

private val DarkColorScheme = darkColorScheme(
    primary = AmberGoldDark,
    onPrimary = Color(0xFF3E2000),
    primaryContainer = Color(0xFF5D3400),
    onPrimaryContainer = Color(0xFFFFDDB3),
    secondary = MintDark,
    onSecondary = Color(0xFF003731),
    secondaryContainer = Color(0xFF005048),
    onSecondaryContainer = Color(0xFFB2DFDB),
    tertiary = BerryDark,
    onTertiary = Color(0xFF3B0944),
    tertiaryContainer = Color(0xFF54215E),
    onTertiaryContainer = Color(0xFFF3E5F5),
    background = NightBackground,
    onBackground = Color(0xFFF5EFE6),
    surface = NightSurface,
    onSurface = Color(0xFFF5EFE6),
    surfaceVariant = NightSurfaceVariant,
    onSurfaceVariant = Color(0xFFD7C8B8)
)

val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(36.dp)
)

@Composable
fun OvoPetTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = AppShapes,
        content = content
    )
}
