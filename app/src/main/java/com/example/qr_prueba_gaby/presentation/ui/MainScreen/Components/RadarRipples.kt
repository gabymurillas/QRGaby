package com.example.qr_prueba_gaby.presentation.ui.MainScreen.Components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.qr_prueba_gaby.presentation.ui.theme.MainBlue

@Composable
fun RadarRipples(rssi: Int, baseAlpha: Float) {
    val infiniteTransition = rememberInfiniteTransition(label = "")
    val proximity = ((rssi + 85).coerceIn(0, 45) / 45f)

    val wave1 by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2000, easing = LinearEasing), RepeatMode.Restart), label = ""
    )
    
    val wave2 by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2000, easing = LinearEasing, delayMillis = 1000), RepeatMode.Restart), label = ""
    )

    Canvas(modifier = Modifier.size(280.dp)) {
        val color = if (rssi > -75) MainBlue else Color.Gray
        
        drawCircle(
            color = color,
            radius = (size.minDimension / 2) * wave1,
            center = center,
            style = Stroke(width = 2.dp.toPx()),
            alpha = (1f - wave1) * baseAlpha * proximity
        )
        
        drawCircle(
            color = color,
            radius = (size.minDimension / 2) * wave2,
            center = center,
            style = Stroke(width = 2.dp.toPx()),
            alpha = (1f - wave2) * baseAlpha * proximity
        )
    }
}
