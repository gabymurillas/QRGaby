package com.example.qr_prueba_gaby.presentation.ui.MainScreen

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.qr_prueba_gaby.presentation.ui.MainScreen.components.*
import com.example.qr_prueba_gaby.presentation.ui.common.BleState
import com.example.qr_prueba_gaby.presentation.ui.theme.*
import com.example.qr_prueba_gaby.data.model.UserData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainViewModel, onLogout: () -> Unit) {
    val bleState by viewModel.bleState.collectAsStateWithLifecycle()
    val odooStatus by viewModel.odooStatus.collectAsStateWithLifecycle()
    val gateMessage by viewModel.gateMessage.collectAsStateWithLifecycle(null)
    val userData by viewModel.userDataFlow.collectAsStateWithLifecycle(null)

    // Estados de seguridad
    val isPinEntryVisible by viewModel.isPinEntryVisible.collectAsStateWithLifecycle()
    val isPinSetupVisible by viewModel.isPinSetupVisible.collectAsStateWithLifecycle()
    val pinError by viewModel.pinError.collectAsStateWithLifecycle()
    
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
                    onError = { viewModel.onBiometricFailureOrPIN() },
                    onCancel = { viewModel.onBiometricFailureOrPIN() }
                )
            } else {
                // Si no hay biometría, ir directo a PIN
                viewModel.onBiometricFailureOrPIN()
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

    LaunchedEffect(Unit) {
        viewModel.unauthorizedEvent.collect {
            onLogout()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = LightGrayBg
    ) { innerPadding ->
        
        // Diálogos de Seguridad
        if (isPinEntryVisible) {
            PinEntryDialog(
                onDismiss = { viewModel.closePinDialogs() },
                onConfirm = { viewModel.validatePin(it) },
                error = pinError
            )
        }
        if (isPinSetupVisible) {
            PinSetupDialog(
                onDismiss = { viewModel.closePinDialogs() },
                onConfirm = { viewModel.setupPin(it) }
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            StatusHeader(
                odooStatus = odooStatus
            )

            Spacer(modifier = Modifier.height(20.dp))

            WelcomeHeader(userName = userData?.u ?: "Usuario")

            Spacer(modifier = Modifier.height(30.dp))

            SystemStatusCard()

            Spacer(modifier = Modifier.weight(1f))

            val isBusy = bleState == BleState.CONNECTING || bleState == BleState.CONNECTED

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
                    color = if (isBusy) SecondaryBlue else MainBlue,
                    shadowElevation = if (isBusy) 12.dp else 4.dp,
                    onClick = { 
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        viewModel.startAuthFlow() 
                    },
                    enabled = !isBusy
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
            Text(
                text = if (isBusy) "Abriendo..." else "Toca para abrir",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MainBlue
            )

            Spacer(modifier = Modifier.height(24.dp))
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
