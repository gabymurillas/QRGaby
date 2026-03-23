package com.example.qr_prueba_gaby.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.qr_prueba_gaby.ui.viewmodel.AppViewModel
import kotlinx.coroutines.delay

@Composable
fun QrSyncScreen(viewModel: AppViewModel, onActivated: () -> Unit) {
    val qrBitmap by viewModel.qrBitmap.collectAsState()
    val isRegistered by viewModel.isRegisteredFlow.collectAsStateWithLifecycle(null)

    // Cuando se activa, espera 2 segundos y navega
    LaunchedEffect(isRegistered) {
        if (isRegistered == true) {
            delay(2_000)
            onActivated()
        }
    }

    val bgGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF0D1117), Color(0xFF0A1628))
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgGradient),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier.padding(horizontal = 32.dp)
        ) {
            // ── Ícono superior ──
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF1565C0).copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.QrCode,
                    contentDescription = null,
                    tint = Color(0xFF42A5F5),
                    modifier = Modifier.size(36.dp)
                )
            }

            // ── Título ──
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Sincroniza tu Acceso",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Muestra este código al administrador\npara activar tu llave digital",
                    fontSize = 14.sp,
                    color = Color(0xFF8899AA),
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )
            }

            // ── QR ──
            Box(
                modifier = Modifier
                    .size(280.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isRegistered == true) {
                    // Overlay de activado: reemplaza el QR con un check verde
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF00C853),
                        modifier = Modifier.size(80.dp)
                    )
                } else if (qrBitmap != null) {
                    Image(
                        bitmap = qrBitmap!!.asImageBitmap(),
                        contentDescription = "Código QR de acceso",
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    CircularProgressIndicator(color = Color(0xFF1565C0))
                }
            }

            // ── Estado ──
            AnimatedContent(
                targetState = isRegistered == true,
                transitionSpec = {
                    fadeIn(tween(400)) togetherWith fadeOut(tween(200))
                },
                label = "statusTransition"
            ) { activated ->
                if (activated) {
                    StatusBadge(
                        text = "¡Acceso Activado!",
                        color = Color(0xFF00C853),
                        pulse = false
                    )
                } else {
                    StatusBadge(
                        text = "Esperando activación del administrador...",
                        color = Color(0xFFFFB300),
                        pulse = true
                    )
                }
            }

            // ── Hint de seguridad ──
            if (isRegistered != true) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF111827)
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🔒", fontSize = 18.sp)
                        Text(
                            text = "Tu identidad está encriptada y protegida en este dispositivo.",
                            fontSize = 12.sp,
                            color = Color(0xFF8899AA),
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(text: String, color: Color, pulse: Boolean) {
    val alpha by if (pulse) {
        rememberInfiniteTransition(label = "pulse").animateFloat(
            initialValue = 0.4f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(900, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulseAlpha"
        )
    } else {
        remember { mutableStateOf(1f) }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(100.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = alpha))
        )
        Text(text, color = color, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}
