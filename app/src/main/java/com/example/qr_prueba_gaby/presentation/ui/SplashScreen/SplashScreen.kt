package com.example.qr_prueba_gaby.presentation.ui.SplashScreen

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.qr_prueba_gaby.R
import com.example.qr_prueba_gaby.presentation.ui.theme.MainBlue
import com.example.qr_prueba_gaby.presentation.ui.theme.isDark
import com.example.qr_prueba_gaby.presentation.ui.theme.textSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(
    isActivated: Boolean?,
    onComplete: (Boolean) -> Unit
) {
    // ── Animatables ──────────────────────────────────────────────────────────
    val haloScale  = remember { Animatable(0f) }
    val haloAlpha  = remember { Animatable(0f) }
    val logoScale  = remember { Animatable(0f) }
    val logoRotate = remember { Animatable(-8f) }
    val textAlpha  = remember { Animatable(0f) }
    val textOffset = remember { Animatable(40f) }

    // Flag para navegar solo una vez, al terminar la animación Y tener isActivated
    var animDone by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        // ── 1. Halo radial — aparece suavemente detrás del logo ──────────────
        launch {
            haloAlpha.animateTo(
                targetValue = 0.18f,
                animationSpec = tween(durationMillis = 700, easing = EaseOut)
            )
        }
        launch {
            haloScale.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 900, easing = EaseOutCubic)
            )
        }

        // ── 2. Logo spring bounce — arranca al mismo tiempo que el halo ──────
        launch {
            logoScale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness    = Spring.StiffnessLow
                )
            )
        }
        launch {
            logoRotate.animateTo(
                targetValue = 0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness    = Spring.StiffnessLow
                )
            )
        }

        // ── 3. Texto — desliza y aparece a los 600 ms ────────────────────────
        delay(600)
        launch {
            textAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 500, easing = EaseOut)
            )
        }
        launch {
            textOffset.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 500, easing = EaseOutCubic)
            )
        }

        // ── Esperar a que termine la duración total antes de navegar ─────────
        delay(1400) // 600 + 1400 = 2 000 ms totales
        animDone = true
    }

    // Navegar en cuanto la animación termine Y tengamos el estado
    LaunchedEffect(animDone, isActivated) {
        if (animDone && isActivated != null) {
            onComplete(isActivated)
        }
    }

    // ── UI ───────────────────────────────────────────────────────────────────
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        // Halo radial detrás del logo — solo en modo claro
        if (!MaterialTheme.colorScheme.isDark) {
            Box(
                modifier = Modifier
                    .size(280.dp)
                    .scale(haloScale.value)
                    .alpha(haloAlpha.value)
                    .background(
                        color = MainBlue,
                        shape = RoundedCornerShape(percent = 50)
                    )
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Logo dentro de tarjeta blanca redondeada
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .graphicsLayer {
                        scaleX   = logoScale.value
                        scaleY   = logoScale.value
                        rotationZ = logoRotate.value
                    }
                    .clip(RoundedCornerShape(32.dp))
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logouser),
                    contentDescription = "Logo",
                    modifier = Modifier
                        .size(100.dp)
                )
            }

            // Texto "Control de Acceso Alcaravan"
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .alpha(textAlpha.value)
                    .offset(y = textOffset.value.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.app_name),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "SISTEMA DE CONTROL",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.textSecondary,
                    letterSpacing = 2.sp
                )
            }
        }
    }
}

// Easing privados — equivalentes a CSS ease-out-cubic / ease-out
private val EaseOut     = CubicBezierEasing(0f, 0f, 0.2f, 1f)
private val EaseOutCubic = CubicBezierEasing(0.33f, 1f, 0.68f, 1f)
