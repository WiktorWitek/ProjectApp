package com.example.travelapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color


private val LightColorScheme = lightColorScheme(
    primary = Color.White,
    background = Color(0xFFF0F9FF),
    secondary = Color(0xFFFF8C42), // przycisk
    surface = Color.White,
    onPrimary = Color.Black, // kolor tekstu
    onSecondary = Color.LightGray,
    onBackground = Color(0xFF03DAC6),
    onSurface = Color.Black
)

private val DarkColorScheme = darkColorScheme(
    primary = Color.Black, // czarny
    secondary = Color(0xFF3700B3), // przycisk
    background = Color(0xFF17202A), // ciemny niebieski
    surface = Color(0xFF1F1B24), // drugi background nie wiem jak opisac ten kolor
    onPrimary = Color.White, //kolor tekstu bialy
    onSecondary = Color.Gray, // drugi kolor tekstu
    onBackground = Color(0xFF03DAC6), // turkusik??
    onSurface = Color.Transparent,
)

@Composable
fun TravelAppTheme(
    darkTheme: Boolean = false, // Przełączanie motywów
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    // Użyj standardowego Typography
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(), // Standardowa konfiguracja Typography
        content = content
    )
}