package com.example.qr_prueba_gaby.presentation.ui.MainScreen

import android.Manifest
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.qr_prueba_gaby.presentation.ui.MainScreen.components.*
import com.example.qr_prueba_gaby.presentation.ui.QrSyncScreen.Components.QrAdminScanner
import com.example.qr_prueba_gaby.presentation.ui.common.GateState
import com.example.qr_prueba_gaby.presentation.ui.common.OdooStatus
import com.example.qr_prueba_gaby.presentation.ui.theme.*
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun MainScreen(viewModel: MainViewModel, onLogout: () -> Unit) {
    val gateState by viewModel.gateState.collectAsStateWithLifecycle()
    val odooStatus by viewModel.odooStatus.collectAsStateWithLifecycle()
    val gateMessage by viewModel.gateMessage.collectAsStateWithLifecycle(null)
    val userData by viewModel.userDataFlow.collectAsStateWithLifecycle(null)

    // Estados de seguridad
    val isPinEntryVisible by viewModel.isPinEntryVisible.collectAsStateWithLifecycle()
    val isPinSetupVisible by viewModel.isPinSetupVisible.collectAsStateWithLifecycle()
    val pinError by viewModel.pinError.collectAsStateWithLifecycle()
    val userPin by viewModel.userPinFlow.collectAsStateWithLifecycle(null)
    val isSettingsVisible by viewModel.isSettingsVisible.collectAsStateWithLifecycle()
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()

    // Estado del re-escaneo del QR del Administrador (cambio de IP del servidor)
    val reprovisionSuccess by viewModel.reprovisionSuccess.collectAsStateWithLifecycle()
    val reprovisionMessage by viewModel.reprovisionMessage.collectAsStateWithLifecycle()
    var showRescanDialog by remember { mutableStateOf(false) }
    var showRescanScanner by remember { mutableStateOf(false) }
    val cameraPermission = rememberPermissionState(Manifest.permission.CAMERA)

    val snackbarHostState = remember { SnackbarHostState() }
    val haptic = LocalHapticFeedback.current
    val context = androidx.compose.ui.platform.LocalContext.current

    // Lanzar Biometría cuando el ViewModel lo indique
    LaunchedEffect(Unit) {
        viewModel.authRequestTrigger.collect {
            val activity = context as? androidx.fragment.app.FragmentActivity
            if (activity != null && com.example.qr_prueba_gaby.utils.BiometricAuthManager.isBiometricAvailable(activity)) {
                com.example.qr_prueba_gaby.utils.BiometricAuthManager.showBiometricPrompt(
                    activity = activity,
                    onSuccess = { viewModel.onAuthSuccess() },
                    onError = { viewModel.onBiometricFailedOrCancelled() },
                    onCancel = { viewModel.onBiometricFailedOrCancelled() }
                )
            } else {
                // El teléfono no tiene huella configurada
                viewModel.onBiometricUnavailable()
            }
        }
    }

    // Recuperación del PIN: verifica al dueño con huella o credencial del teléfono
    LaunchedEffect(Unit) {
        viewModel.pinRecoveryTrigger.collect {
            val activity = context as? androidx.fragment.app.FragmentActivity
            if (activity != null && com.example.qr_prueba_gaby.utils.BiometricAuthManager.canVerifyOwner(activity)) {
                com.example.qr_prueba_gaby.utils.BiometricAuthManager.showOwnerVerificationPrompt(
                    activity = activity,
                    onSuccess = { viewModel.onPinRecoveryVerified() },
                    onFailed = { /* el usuario puede reintentar desde el diálogo */ }
                )
            } else {
                // El teléfono no tiene huella ni bloqueo → restablecer directo
                viewModel.onPinRecoveryVerified()
            }
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

    // Cierra el diálogo de actualización automáticamente tras un re-escaneo exitoso
    LaunchedEffect(reprovisionSuccess) {
        if (reprovisionSuccess == true) {
            delay(1800)
            showRescanDialog = false
            viewModel.resetReprovisionState()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->

        // Diálogos de Seguridad
        if (isPinEntryVisible) {
            PinEntryDialog(
                onDismiss = { viewModel.closePinDialogs() },
                onConfirm = { viewModel.validatePin(it) },
                error = pinError,
                onForgotPin = { viewModel.startPinRecovery() }
            )
        }
        if (isPinSetupVisible) {
            PinSetupDialog(
                onDismiss = { viewModel.closePinDialogs() },
                onConfirm = { viewModel.setupPin(it) }
            )
        }

        // Modal de ajustes de seguridad (PIN opcional)
        if (isSettingsVisible) {
            SettingsDialog(
                hasPin = userPin != null,
                onTogglePin = { enabled -> viewModel.setPinEnabled(enabled) },
                onChangePin = { viewModel.requestChangePin() },
                themeMode = themeMode,
                onThemeModeChange = { viewModel.setThemeMode(it) },
                onDismiss = { viewModel.closeSettings() }
            )
        }

        // ── Modal: actualizar el servidor re-escaneando el QR del Administrador ──
        if (showRescanDialog) {
            AlertDialog(
                onDismissRequest = {
                    showRescanDialog = false
                    viewModel.resetReprovisionState()
                },
                icon = { Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = MainBlue) },
                title = { Text("Actualizar servidor") },
                text = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Si cambió la IP o la dirección del servidor, vuelve a escanear el QR del administrador para reconectar la app.",
                            textAlign = TextAlign.Center
                        )
                        reprovisionMessage?.let { msg ->
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = msg,
                                color = if (reprovisionSuccess == true) Color(0xFF16A34A) else Color(0xFFDC2626),
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        if (cameraPermission.status.isGranted) {
                            viewModel.resetReprovisionState()
                            showRescanScanner = true
                        } else {
                            cameraPermission.launchPermissionRequest()
                        }
                    }) { Text("Escanear QR") }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showRescanDialog = false
                        viewModel.resetReprovisionState()
                    }) { Text("Cerrar") }
                }
            )
        }

        // ── Escáner QR para el re-aprovisionamiento ──
        if (showRescanScanner) {
            Dialog(
                onDismissRequest = { showRescanScanner = false },
                properties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
                    QrAdminScanner(
                        modifier = Modifier.fillMaxSize(),
                        onQrDetected = { raw ->
                            showRescanScanner = false
                            viewModel.reprovision(raw)
                        }
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .align(Alignment.TopStart),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { showRescanScanner = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Cerrar escáner", tint = Color.White)
                        }
                        Text(
                            text = "Escanea el QR del Administrador",
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            StatusHeader(
                odooStatus = odooStatus,
                onSettingsClick = { viewModel.openSettings() }
            )

            Spacer(modifier = Modifier.height(20.dp))

            WelcomeHeader(userName = userData?.u ?: "Usuario")

            Spacer(modifier = Modifier.height(30.dp))

            SystemStatusCard(odooStatus = odooStatus)

            Spacer(modifier = Modifier.weight(1f))

            val isBusy = gateState == GateState.REQUESTING
            // Conductor no encontrado en Odoo: la apertura no procede.
            val isNotFound = odooStatus == OdooStatus.INVALID

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

                Surface(
                    modifier = Modifier
                        .size(160.dp)
                        .scale(pulseScale),
                    shape = CircleShape,
                    color = when {
                        isBusy     -> SecondaryBlue
                        isNotFound -> Color(0xFF9CA3AF)
                        else       -> MainBlue
                    },
                    shadowElevation = if (isBusy) 12.dp else 4.dp,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        viewModel.startAuthFlow()
                    },
                    enabled = !isBusy && !isNotFound
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (isBusy) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(40.dp))
                        } else {
                            Icon(
                                imageVector = Icons.Default.LockOpen,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = Color.White
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            if (isNotFound) {
                // Conductor no registrado en Odoo: ofrecer volver al registro.
                Button(
                    onClick = { onLogout() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Logout,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Tocar para salir", fontWeight = FontWeight.Bold, color = Color.White)
                }
            } else {
                Text(
                    text = if (isBusy) "Abriendo..." else "Toca para abrir",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MainBlue
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = { showRescanDialog = true }) {
                Icon(
                    imageVector = Icons.Default.Sync,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.textSecondary
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Actualizar servidor",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.textSecondary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            Spacer(modifier = Modifier.weight(1f))
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
