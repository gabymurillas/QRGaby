package com.example.qr_prueba_gaby.presentation.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ── Tema Claro: idéntico a la paleta original (sin cambios visuales) ──
private val LightColorScheme = lightColorScheme(
    primary          = MainBlue,
    onPrimary        = Color.White,
    primaryContainer = SecondaryBlue,
    secondary        = NavyBlue,
    onSecondary      = Color.White,
    tertiary         = GreenAccent,
    background       = LightGrayBg,
    onBackground     = Color.Black,
    surface          = Color.White,
    onSurface        = Color.Black,
    surfaceVariant   = InputBg,
    error            = RedAccent,
    outline          = TextGray
)

// ── Tema Oscuro: misma identidad azul, superficies neutras Material 3 ──
private val DarkColorScheme = darkColorScheme(
    primary            = BlueLight,
    onPrimary          = Color(0xFF06243D),
    primaryContainer   = SecondaryBlue,
    onPrimaryContainer = Color.White,
    secondary          = BlueLight,
    onSecondary        = Color(0xFF06243D),
    tertiary           = GreenAccent,
    background         = DarkBackground,
    onBackground       = DarkOnSurface,
    surface            = DarkSurface,
    onSurface          = DarkOnSurface,
    surfaceVariant     = DarkSurfaceVariant,
    onSurfaceVariant   = DarkTextSecondary,
    error              = RedAccent,
    outline            = DarkOutline
)

@Composable
fun QRPRUEBAGABYTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = android.graphics.Color.TRANSPARENT
            window.navigationBarColor = android.graphics.Color.TRANSPARENT
            // Iconos oscuros sobre fondo claro; iconos claros sobre fondo oscuro.
            val lightBars = !darkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = lightBars
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = lightBars
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
