package com.example.qr_prueba_gaby.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.qr_prueba_gaby.ui.viewmodel.AppViewModel
import kotlinx.coroutines.delay

@Composable
fun QrSyncScreen(
    viewModel: AppViewModel,
    onActivated: () -> Unit,
    onReset: () -> Unit
) {
    val qrBitmap by viewModel.qrBitmap.collectAsState()
    val isRegistered by viewModel.isRegisteredFlow.collectAsStateWithLifecycle(null)
    val isActivated by viewModel.isActivatedFlow.collectAsStateWithLifecycle(null)
    val decryptedId by viewModel.decryptedAndroidId.collectAsStateWithLifecycle(null)
    val isValidating by viewModel.isValidating.collectAsStateWithLifecycle(false)
    var validationError by remember { mutableStateOf<String?>(null) }
    val clipboardManager = LocalClipboardManager.current

    // Iniciar/detener el Peripheral de activación BLE
    DisposableEffect(Unit) {
        viewModel.startActivationPeripheral()
        onDispose { viewModel.stopActivationPeripheral() }
    }

    // Cuando se activa, espera 2 segundos y navega
    LaunchedEffect(isActivated) {
        if (isActivated == true) {
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

            // ── Botón de Validación Manual ──
            if (isActivated != true) {
                Button(
                    onClick = { 
                        validationError = null
                        viewModel.validateOnEndpoint(
                            onSuccess = { onActivated() },
                            onError = { validationError = it }
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1565C0),
                        disabledContainerColor = Color(0xFF1A2332)
                    ),
                    enabled = !isValidating
                ) {
                    if (isValidating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(Modifier.width(12.dp))
                        Text("Verificando...", fontWeight = FontWeight.SemiBold)
                    } else {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Spacer(Modifier.width(10.dp))
                        Text("Validar Acceso", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Mostrar error de validación si existe
                AnimatedVisibility(visible = validationError != null) {
                    Text(
                        text = validationError ?: "",
                        color = Color(0xFFEF5350),
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }

            // ── Estado ──
            AnimatedContent(
                targetState = isActivated == true,
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
                        text = "Esperando validación...",
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

            // ── Botón Mostrar ID (Modo Manual) ──
            if (isActivated != true) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    TextButton(
                        onClick = { viewModel.showDecryptedId() },
                        colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF42A5F5))
                    ) {
                        Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Ver mi ID Android (Manual)", fontSize = 12.sp)
                    }

                    AnimatedVisibility(
                        visible = decryptedId != null,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        decryptedId?.let { id ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1A2332)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.padding(top = 8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text(
                                        text = id,
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.weight(1f, fill = false)
                                    )
                                    IconButton(
                                        onClick = { clipboardManager.setText(AnnotatedString(id)) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.ContentCopy,
                                            contentDescription = "Copiar",
                                            tint = Color.Cyan,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ── Enlace para volver/editar ──
            if (isActivated != true) {
                Spacer(Modifier.height(8.dp))
                TextButton(
                    onClick = { viewModel.resetRegistration { onReset() } }
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Volver a Empezar / Editar Datos", fontSize = 12.sp, color = Color(0xFF8899AA))
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
