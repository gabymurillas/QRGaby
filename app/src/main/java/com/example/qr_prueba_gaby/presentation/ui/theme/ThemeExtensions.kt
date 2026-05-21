package com.example.qr_prueba_gaby.presentation.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

/**
 * Extensiones de color derivadas del [ColorScheme] activo.
 *
 * Permiten que los componentes adapten colores que no encajan en un token
 * estándar de Material 3, sin alterar el aspecto del tema claro.
 */

/** true cuando el esquema de color activo es el tema oscuro. */
val ColorScheme.isDark: Boolean
    get() = background.luminance() < 0.5f

/**
 * Gris de texto secundario adaptado al tema: conserva [TextGray] en claro
 * y usa un gris claro legible ([DarkTextSecondary]) en oscuro.
 */
val ColorScheme.textSecondary: Color
    get() = if (isDark) DarkTextSecondary else TextGray
