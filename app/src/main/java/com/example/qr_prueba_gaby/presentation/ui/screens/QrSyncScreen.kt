package com.example.qr_prueba_gaby.presentation.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import com.example.qr_prueba_gaby.presentation.ui.theme.*
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.qr_prueba_gaby.presentation.ui.viewmodels.AppViewModel
import kotlinx.coroutines.delay

@Composable
fun QrSyncScreen(
    viewModel: AppViewModel,
    onActivated: () -> Unit,
    onReset: () -> Unit
) {
    val qrBitmap by viewModel.qrBitmap.collectAsState()
    val isActivated by viewModel.isActivatedFlow.collectAsStateWithLifecycle(null)
    val nombre by viewModel.nombre.collectAsState()
    val cedula by viewModel.cedula.collectAsState()
    val isValidating by viewModel.isValidating.collectAsStateWithLifecycle(false)
    var validationError by remember { mutableStateOf<String?>(null) }

    // Cuando se activa, espera 2 segundos y navega
    LaunchedEffect(isActivated) {
        if (isActivated == true) {
            delay(2000)
            onActivated()
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(LightGrayBg)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(top = 20.dp, bottom = 40.dp)
    ) {
        // ── Top Bar ──
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { onReset() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = NavyBlue)
                }
                Text(
                    text = "EscanQR",
                    color = NavyBlue,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // ── Titles ──
        item {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "PORTAL DE ACCESO",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextGray,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Muestra este código al\nadministrador",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = NavyBlue,
                    textAlign = TextAlign.Center,
                    lineHeight = 32.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                // Decorative divider line
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(3.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFD1D5DB))
                )
            }
            Spacer(modifier = Modifier.height(30.dp))
        }

        // ── Main Card (QR + Info) ──
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = 12.dp, shape = RoundedCornerShape(32.dp), spotColor = Color(0x22000000)),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // QR Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(24.dp))
                            .background(InputBg)
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (qrBitmap != null) {
                            Image(
                                bitmap = qrBitmap!!.asImageBitmap(),
                                contentDescription = "QR",
                                modifier = Modifier.fillMaxSize()
                            )
                            QRBracketsOverlay()
                        } else {
                            CircularProgressIndicator(color = NavyBlue)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // User Info
                    Text(
                        text = nombre.ifBlank { "Usuario" },
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1F2937)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "C.I: V-$cedula",
                        fontSize = 14.sp,
                        color = TextGray,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Status Badge
                    StatusBadge(
                        text = if (isActivated == true) "ACCESO ACTIVADO" else "ESPERANDO VALIDACIÓN",
                        color = if (isActivated == true) Color(0xFF00C853) else NavyBlue,
                        pulse = isActivated != true
                    )
                }
            }
            Spacer(modifier = Modifier.height(40.dp))
        }

        // ── Action Button ──
        item {
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
                    .height(64.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NavyBlue),
                enabled = !isValidating && isActivated != true
            ) {
                if (isValidating) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                } else {
                    Icon(
                        imageVector = if (isActivated == true) Icons.Default.Check else Icons.Default.Sync,
                        contentDescription = null,
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = if (isActivated == true) "ACTIVADO" else "Verificar",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
            
            // Error Display
            AnimatedVisibility(visible = validationError != null) {
                Text(
                    text = validationError ?: "",
                    color = Color(0xFFEF5350),
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }
            
            // Optional "Volver" as TextButton if needed, or if the main button is "Verificar"
            // the user said "cambiar botón volver por botón verificar".
            // If they want to go back to edit:
            TextButton(
                onClick = { onReset() },
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text("Volver a editar perfil", color = TextGray)
            }
        }
    }
}

@Composable
private fun QRBracketsOverlay() {
    Box(modifier = Modifier.fillMaxSize()) {
        val color = NavyBlue.copy(alpha = 0.8f)
        val thickness = 3.dp
        val size = 20.dp
        
        // Top Left
        Box(modifier = Modifier.align(Alignment.TopStart).size(size)) {
            Box(modifier = Modifier.fillMaxWidth().height(thickness).background(color))
            Box(modifier = Modifier.fillMaxHeight().width(thickness).background(color))
        }
        // Top Right
        Box(modifier = Modifier.align(Alignment.TopEnd).size(size)) {
            Box(modifier = Modifier.fillMaxWidth().height(thickness).background(color))
            Box(modifier = Modifier.fillMaxHeight().width(thickness).background(color).align(Alignment.TopEnd))
        }
        // Bottom Left
        Box(modifier = Modifier.align(Alignment.BottomStart).size(size)) {
            Box(modifier = Modifier.fillMaxWidth().height(thickness).background(color).align(Alignment.BottomStart))
            Box(modifier = Modifier.fillMaxHeight().width(thickness).background(color))
        }
        // Bottom Right
        Box(modifier = Modifier.align(Alignment.BottomEnd).size(size)) {
            Box(modifier = Modifier.fillMaxWidth().height(thickness).background(color).align(Alignment.BottomEnd))
            Box(modifier = Modifier.fillMaxHeight().width(thickness).background(color).align(Alignment.BottomEnd))
        }
    }
}

@Composable
private fun StatusBadge(text: String, color: Color, pulse: Boolean) {
    val alpha by if (pulse) {
        rememberInfiniteTransition(label = "pulseAlpha").animateFloat(
            initialValue = 0.5f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = ""
        )
    } else {
        remember { mutableStateOf(1f) }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(100.dp))
            .background(color.copy(alpha = 0.1f))
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = alpha))
        )
        Text(
            text = text,
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
    }
}
