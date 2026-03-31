package com.example.qr_prueba_gaby.presentation.ui.MainScreen.Components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import com.example.qr_prueba_gaby.presentation.ui.theme.MainBlue
import com.example.qr_prueba_gaby.presentation.ui.theme.SecondaryBlue

@Composable
fun ProximityCircularBar(rssi: Int) {
    // Mapear RSSI (-100 a -40) a progreso (0.0 a 1.0)
    // -100 o menos -> 0.0
    // -40 o más -> 1.0
    val targetProgress = ((rssi + 100).coerceAtLeast(0).toFloat() / 60f).coerceIn(0f, 1f)
    
    val animatedProgress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = tween(durationMillis = 500),
        label = "proximityProgress"
    )

    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(200.dp)) {
        // Fondo de la barra (gris oscuro o azul muy suave)
        CircularProgressIndicator(
            progress = { 1f },
            modifier = Modifier.size(190.dp),
            color = Color.White.copy(alpha = 0.1f),
            strokeWidth = 8.dp,
            strokeCap = StrokeCap.Round,
        )
        
        // Barra de progreso activa
        CircularProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier.size(190.dp),
            color = if (animatedProgress > 0.7f) MainBlue else SecondaryBlue.copy(alpha = 0.6f),
            strokeWidth = 8.dp,
            strokeCap = StrokeCap.Round,
        )
    }
}
