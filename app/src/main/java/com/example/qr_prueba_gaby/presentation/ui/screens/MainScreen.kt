package com.example.qr_prueba_gaby.presentation.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ExitToApp
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.qr_prueba_gaby.presentation.ui.viewmodels.AppViewModel
import com.example.qr_prueba_gaby.presentation.ui.viewmodels.BleState
import com.example.qr_prueba_gaby.presentation.ui.theme.*

@Composable
fun MainScreen(
    viewModel: AppViewModel,
    onLogout: () -> Unit
) {
    val bleState by viewModel.bleState.collectAsStateWithLifecycle(BleState.DISCONNECTED)
    val gateMessage by viewModel.gateMessage.collectAsStateWithLifecycle(null)
    val userData by viewModel.userDataFlow.collectAsStateWithLifecycle(null)
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(gateMessage) {
        gateMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearGateMessage()
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
            // ── Top Bar ──
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = MainBlue, modifier = Modifier.size(28.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "EscanQR", color = MainBlue, fontSize = 18.sp, fontWeight = FontWeight.Black)
                }
                IconButton(onClick = { viewModel.resetRegistration { onLogout() } }) {
                    Icon(Icons.Outlined.ExitToApp, contentDescription = "Cerrar Sesión", tint = TextGray)
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            // ── Welcome ──
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "BIENVENIDO DE NUEVO", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextGray, letterSpacing = 1.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = userData?.u ?: "Usuario", fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = MainBlue, textAlign = TextAlign.Center)
            }

            Spacer(modifier = Modifier.height(40.dp))

            // ── Status Card M3 ──
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

            // ── Action Button M3 ──
            val isBusy = bleState == BleState.CONNECTING || bleState == BleState.CONNECTED
            
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Usamos un FloatingActionButton grande o un Button elevado para no "inventar" formas
                FilledIconButton(
                    onClick = { viewModel.openGate() },
                    modifier = Modifier.size(160.dp),
                    shape = CircleShape,
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = if (isBusy) SecondaryBlue else MainBlue),
                    enabled = !isBusy
                ) {
                    if (isBusy) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(40.dp))
                    } else {
                        Icon(Icons.Default.LockOpen, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.White)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = if (isBusy) "Conectando..." else "Abrir Portón",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MainBlue
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // ── Vehicle Badge ──
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
