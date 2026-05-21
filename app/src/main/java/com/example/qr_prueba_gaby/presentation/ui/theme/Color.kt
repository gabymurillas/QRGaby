package com.example.qr_prueba_gaby.presentation.ui.theme

import androidx.compose.ui.graphics.Color

// ── Tu Paleta Original (Manteniendo nombres para no romper nada) ──
val BluePrimary     = Color(0xFF1565C0)
val BlueLight       = Color(0xFF42A5F5)
val BlueDeep        = Color(0xFF0D47A1)

// ── Fondo (Estos son los que usan los diálogos) ──
val BackgroundDark  = Color(0xFF0D1117)
val SurfaceDark     = Color(0xFF111827) // Este es el que pedía el diálogo
val SurfaceAlt      = Color(0xFF1A2332)

// ── Acentos (Actualizados a los que pedías para mejor visibilidad) ──
val GreenAccent     = Color(0xFF00C853)
val YellowAccent    = Color(0xFFFFB300)
val RedAccent       = Color(0xFFEF5350)

// ── Texto ──
val TextPrimary     = Color(0xFFFFFFFF)
val TextSecondary   = Color(0xFF8899AA) // Agregado: este faltaba y causaba error
val TextHint        = Color(0xFF445566)
val TextGray        = Color(0xFF6B7280)

// ── Bordes ──
val BorderColor     = Color(0xFF223344)

// ── New Design Colors (Ajustados para que coincidan con tus componentes) ──
val NavyBlue        = Color(0xFF0F1B61)
val MainBlue        = Color(0xFF2196F3) // Cambiado a azul brillante para los botones
val SecondaryBlue   = Color(0xFF414467)
val LightGrayBg     = Color(0xFFFAFAFA)
val InputBg         = Color(0xFFF0F2F5)
val CardBg          = Color(0xFFF3F4F6)

// ── Modo Oscuro (Material Design 3) — superficies neutras, misma identidad azul ──
val DarkBackground     = Color(0xFF121212) // Fondo de las pantallas principales
val DarkSurface        = Color(0xFF1E1E1E) // Tarjetas y superficies elevadas
val DarkSurfaceVariant = Color(0xFF2E2E2E) // Inputs y superficies secundarias
val DarkOnSurface      = Color(0xFFECEFF4) // Texto principal sobre fondo oscuro
val DarkTextSecondary  = Color(0xFF9AA5B1) // Texto secundario sobre fondo oscuro
val DarkOutline        = Color(0xFF3A4250) // Bordes en tema oscuro