package com.example.qr_prueba_gaby.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.qr_prueba_gaby.ui.viewmodel.AppViewModel
import com.example.qr_prueba_gaby.ui.viewmodel.BleState

@Composable
fun MainScreen(viewModel: AppViewModel) {
    val context = LocalContext.current
    val bleState by viewModel.bleState.collectAsStateWithLifecycle(BleState.SEARCHING)
    val gateMessage by viewModel.gateMessage.collectAsStateWithLifecycle(null)
    val userData by viewModel.userDataFlow.collectAsStateWithLifecycle(null)

    val snackbarHostState = remember { SnackbarHostState() }

    // Inicia escaneo BLE al entrar y lo detiene al salir
    DisposableEffect(Unit) {
        viewModel.startBleScan()
        onDispose { viewModel.stopBleScan() }
    }

    // Muestra Snackbar cuando hay mensaje de portón
    LaunchedEffect(gateMessage) {
        gateMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearGateMessage()
        }
    }

    val bgGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF0D1117), Color(0xFF0A1628))
    )

    Scaffold(
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = Color(0xFF00C853),
                    contentColor = Color.White,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        containerColor = Color.Transparent
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(bgGradient)
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {

                // ── Saludo ──
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Hola, ${userData?.u?.substringBefore(" ") ?: "Usuario"} 👋",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(Modifier.height(8.dp))
                    ActiveBadge()
                }

                Spacer(Modifier.height(48.dp))

                // ── Botón central BLE ──
                GateButton(bleState = bleState, onPress = { viewModel.openGate() })

                Spacer(Modifier.height(48.dp))

                // ── Vehículos ──
                userData?.p?.takeIf { it.isNotEmpty() }?.let { plates ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Mis Vehículos",
                            fontSize = 13.sp,
                            color = Color(0xFF8899AA),
                            modifier = Modifier.padding(bottom = 10.dp)
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(plates) { plate ->
                                PlateChip(plate = plate)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(32.dp))

                // ── Estado BLE ──
                BleStatusRow(bleState = bleState)
            }
        }
    }
}

@Composable
private fun ActiveBadge() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(100.dp))
            .background(Color(0xFF00C853).copy(alpha = 0.15f))
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(Color(0xFF00C853))
        )
        Text("Estado: Activo", color = Color(0xFF00C853), fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun GateButton(bleState: BleState, onPress: () -> Unit) {
    val isInRange = bleState == BleState.IN_RANGE
    val isConnecting = bleState == BleState.CONNECTING || bleState == BleState.SENT

    // Animación de pulso cuando está en rango
    val scale by rememberInfiniteTransition(label = "pulse").animateFloat(
        initialValue = 1f,
        targetValue = if (isInRange) 1.07f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "gateScale"
    )

    val btnColor = when {
        isConnecting -> Color(0xFF37474F)
        isInRange    -> Color(0xFF1565C0)
        else         -> Color(0xFF263238)
    }

    val ringColor = if (isInRange) Color(0xFF42A5F5).copy(alpha = 0.25f) else Color.Transparent

    Box(contentAlignment = Alignment.Center) {
        // Anillo exterior de pulso
        Box(
            modifier = Modifier
                .size(200.dp)
                .clip(CircleShape)
                .background(ringColor)
                .scale(scale)
        )

        // Botón principal
        Button(
            onClick = onPress,
            modifier = Modifier.size(160.dp),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = btnColor,
                disabledContainerColor = Color(0xFF1A2332)
            ),
            enabled = isInRange
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (isConnecting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(32.dp),
                        color = Color.White,
                        strokeWidth = 3.dp
                    )
                    Spacer(Modifier.height(8.dp))
                    Text("Enviando...", fontSize = 12.sp, color = Color.White, textAlign = TextAlign.Center)
                } else {
                    Icon(
                        imageVector = if (isInRange) Icons.Default.LockOpen else Icons.Default.Lock,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = if (isInRange) "Abrir Portón" else "Buscar portón",
                        fontSize = 14.sp,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun PlateChip(plate: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF111827))
            .border(1.dp, Color(0xFF223344), RoundedCornerShape(10.dp))
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Icon(
            Icons.Default.DirectionsCar,
            contentDescription = null,
            tint = Color(0xFF42A5F5),
            modifier = Modifier.size(16.dp)
        )
        Text(plate, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun BleStatusRow(bleState: BleState) {
    val (icon, text, color) = when (bleState) {
        BleState.SEARCHING  -> Triple(Icons.Default.Search,   "Buscando portón...",      Color(0xFF8899AA))
        BleState.IN_RANGE   -> Triple(Icons.Default.LockOpen, "Portón en rango",          Color(0xFF42A5F5))
        BleState.CONNECTING -> Triple(Icons.Default.Lock,     "Conectando...",            Color(0xFFFFB300))
        BleState.SENT       -> Triple(Icons.Default.LockOpen, "Señal enviada",            Color(0xFF00C853))
        BleState.ERROR      -> Triple(Icons.Default.Lock,     "Error de Bluetooth",       Color(0xFFEF5350))
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
        Text(text, color = color, fontSize = 13.sp)
    }
}
