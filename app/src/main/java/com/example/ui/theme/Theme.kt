package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = WarmAmberAccent,
    onPrimary = Color.Black,
    primaryContainer = WarmOrangeVariant,
    onPrimaryContainer = Color.White,
    secondary = WarmGold,
    onSecondary = Color.Black,
    background = DarkBackground,
    onBackground = Color(0xFFFBE9E7),
    surface = DarkSurface,
    onSurface = Color(0xFFFBE9E7),
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFFFCCBC),
    outline = Color(0xFF8D6E63)
)

private val LightColorScheme = lightColorScheme(
    primary = WarmOrangePrimary,
    onPrimary = Color.White,
    primaryContainer = CreamSurfaceVariant,
    onPrimaryContainer = WarmDarkBrown,
    secondary = WarmAmberAccent,
    onSecondary = Color.White,
    tertiary = WarmGold,
    onTertiary = Color.Black,
    background = CreamBackground,
    onBackground = WarmDarkBrown,
    surface = CreamSurface,
    onSurface = WarmDarkBrown,
    surfaceVariant = CreamSurfaceVariant,
    onSurfaceVariant = WarmMediumBrown,
    outline = WarmBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
