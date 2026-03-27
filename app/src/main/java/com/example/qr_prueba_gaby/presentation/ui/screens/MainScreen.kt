package com.example.qr_prueba_gaby.presentation.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.qr_prueba_gaby.presentation.ui.viewmodels.MainViewModel
import com.example.qr_prueba_gaby.presentation.ui.viewmodels.BleState
import com.example.qr_prueba_gaby.presentation.ui.viewmodels.OdooStatus
import com.example.qr_prueba_gaby.presentation.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainViewModel, onLogout: () -> Unit) {
    val bleState by viewModel.bleState.collectAsStateWithLifecycle()
    val rssi by viewModel.rssi.collectAsStateWithLifecycle()
    val odooStatus by viewModel.odooStatus.collectAsStateWithLifecycle()
    val gateMessage by viewModel.gateMessage.collectAsStateWithLifecycle(null)
    val userData by viewModel.userDataFlow.collectAsStateWithLifecycle(null)
    val decryptedId by viewModel.decryptedAndroidId.collectAsStateWithLifecycle()
    val idProgress by viewModel.idVisibilityProgress.collectAsStateWithLifecycle()
    
    val snackbarHostState = remember { SnackbarHostState() }
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(rssi) {
        if (rssi > -65) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    LaunchedEffect(gateMessage) {
        gateMessage?.let {
            snackbarHostState.showSnackbar(it)
            if (it.contains("Concedido")) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            }
            viewModel.clearGateMessage()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.unauthorizedEvent.collect {
            onLogout()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = LightGrayBg
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(
                                when (odooStatus) {
                                    OdooStatus.VALID -> Color(0xFF00C853)
                                    OdooStatus.INVALID -> Color.Red
                                    OdooStatus.VERIFYING -> Color.Yellow
                                    else -> Color.Gray
                                }
                            )
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = if (odooStatus == OdooStatus.VALID) "Sincronizado" else "Sincronizando...",
                        fontSize = 12.sp,
                        color = TextGray,
                        fontWeight = FontWeight.Medium
                    )
                }
                IconButton(onClick = { viewModel.logout { onLogout() } }) {
                    Icon(Icons.Outlined.ExitToApp, contentDescription = "Cerrar", tint = TextGray)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "BIENVENIDO DE NUEVO", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextGray, letterSpacing = 1.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = userData?.u ?: "Usuario", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = MainBlue, textAlign = TextAlign.Center)
            }

            Spacer(modifier = Modifier.height(30.dp))

            ElevatedCard(
                modifier = Modifier.fillMaxWidth().height(90.dp),
                shape = RoundedCornerShape(24.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp), contentAlignment = Alignment.CenterStart) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier.size(48.dp),
                            shape = RoundedCornerShape(14.dp),
                            color = MainBlue
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Security, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("ESTADO DEL SISTEMA", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextGray)
                            Text("Activo", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MainBlue)
                        }
                    }
                    Badge(
                        modifier = Modifier.align(Alignment.TopEnd).padding(top = 16.dp, end = 16.dp),
                        containerColor = Color(0xFFE0E7FF),
                        contentColor = Color(0xFF4338CA)
                    ) {
                        Text("VERIFICADO", modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), fontSize = 9.sp, fontWeight = FontWeight.Black)
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            val isBusy = bleState == BleState.CONNECTING || bleState == BleState.CONNECTED
            val isReady = rssi > -75
            
            val infiniteTransition = rememberInfiniteTransition(label = "pulse")
            val pulseScale by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = if (isBusy) 1.15f else 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(800, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "pulseScale"
            )

            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(240.dp)) {
                val radarAlpha by animateFloatAsState(targetValue = if (isReady) 1f else 0.2f, label = "")
                RadarRipples(rssi, radarAlpha)

                Surface(
                    modifier = Modifier
                        .size(160.dp)
                        .scale(pulseScale),
                    shape = CircleShape,
                    color = if (isBusy) SecondaryBlue else if (isReady) MainBlue else Color.Gray,
                    shadowElevation = if (isBusy) 12.dp else 4.dp,
                    onClick = { if (isReady) { haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove); viewModel.openGate() } },
                    enabled = !isBusy && isReady
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (isBusy) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(40.dp))
                        } else {
                            Icon(
                                imageVector = if (isReady) Icons.Default.LockOpen else Icons.Default.Lock,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = Color.White
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = when {
                    isBusy -> "Abriendo..."
                    rssi <= -80 -> "Buscando portón..."
                    rssi in -79..-71 -> "Acércate más"
                    else -> "¡En rango! Toca para abrir"
                },
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (isReady) MainBlue else TextGray
            )

            Spacer(modifier = Modifier.height(24.dp))

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                AnimatedVisibility(
                    visible = decryptedId != null,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(
                            color = Color(0xFFF1F5F9),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("ID de Dispositivo", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextGray)
                                Text(decryptedId ?: "", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = MainBlue)
                            }
                        }
                        LinearProgressIndicator(
                            progress = { idProgress },
                            modifier = Modifier.width(120.dp).height(4.dp).clip(CircleShape),
                            color = MainBlue,
                            trackColor = Color(0xFFE2E8F0)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                OutlinedButton(
                    onClick = { userData?.let { viewModel.showDecryptedId(it.aid) } },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MainBlue)
                ) {
                    Icon(if (decryptedId == null) Icons.Default.Visibility else Icons.Default.VisibilityOff, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (decryptedId == null) "VER ID DISPOSITIVO" else "OCULTAR ID", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            userData?.p?.firstOrNull()?.let { plate ->
                AssistChip(
                    onClick = { },
                    label = { Text(plate, fontWeight = FontWeight.Bold) },
                    leadingIcon = { Icon(Icons.Default.DirectionsCar, contentDescription = null, modifier = Modifier.size(18.dp), tint = MainBlue) },
                    modifier = Modifier.padding(bottom = 32.dp)
                )
            }
        }
    }
}

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
