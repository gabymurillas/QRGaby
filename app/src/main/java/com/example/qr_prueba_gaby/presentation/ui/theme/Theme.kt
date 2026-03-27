package com.example.qr_prueba_gaby.presentation.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

private val AppColorScheme = lightColorScheme(
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

@Composable
fun QRPRUEBAGABYTheme(content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = android.graphics.Color.TRANSPARENT
            window.navigationBarColor = android.graphics.Color.TRANSPARENT
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = true
        }
    }

    MaterialTheme(
        colorScheme = AppColorScheme,
        typography = Typography,
        content = content
    )
}