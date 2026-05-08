package com.example.qr_prueba_gaby.presentation.ui.QrSyncScreen

import androidx.compose.animation.*
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.qr_prueba_gaby.presentation.ui.QrSyncScreen.Components.*
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.delay

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun QrSyncScreen(
    viewModel: SyncViewModel,
    onActivated: () -> Unit,
    onReset: () -> Unit
) {
    val qrBitmap          by viewModel.qrBitmap.collectAsState()
    val isActivated       by viewModel.isActivatedFlow.collectAsStateWithLifecycle(null)
    val userData          by viewModel.userDataFlow.collectAsStateWithLifecycle(null)
    val isValidating      by viewModel.isValidating.collectAsStateWithLifecycle(false)
    val provSuccess       by viewModel.provisioningSuccess.collectAsStateWithLifecycle()
    val provMessage       by viewModel.provisioningMessage.collectAsStateWithLifecycle()

    val decryptedId       by viewModel.decryptedAndroidId.collectAsStateWithLifecycle()
    val idProgress        by viewModel.idVisibilityProgress.collectAsStateWithLifecycle()

    var validationError   by remember { mutableStateOf<String?>(null) }

    // ── Permiso de cámara ────────────────────────────────────────────────────
    val cameraPermission  = rememberPermissionState(android.Manifest.permission.CAMERA)

    // ── Estado del diálogo del escáner ───────────────────────────────────────
    var showScanner       by remember { mutableStateOf(false) }

    // ── Navegación automática al activarse ──────────────────────────────────
    LaunchedEffect(isActivated) {
        if (isActivated == true) {
            delay(2000)
            onActivated()
        }
    }

    // ── Diálogo de escáner QR ────────────────────────────────────────────────
    if (showScanner) {
        Dialog(
            onDismissRequest = {
                showScanner = false
                viewModel.resetProvisioningState()
            },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                // Vista de cámara
                QrAdminScanner(
                    modifier = Modifier.fillMaxSize(),
                    onQrDetected = { raw ->
                        showScanner = false
                        viewModel.processProvisioningQr(raw)
                    }
                )

                // Barra superior del escáner
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .align(Alignment.TopStart),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            showScanner = false
                            viewModel.resetProvisioningState()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar escáner",
                            tint = Color.White
                        )
                    }
                    Text(
                        text = "Apunta al QR del Administrador",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                }

                // Marco guía centrado
                Box(
                    modifier = Modifier
                        .size(240.dp)
                        .align(Alignment.Center)
                        .background(Color.Transparent)
                ) {
                    // Esquinas del marco de guía
                    val cornerColor = Color(0xFF60A5FA)
                    val cornerSize  = 28.dp
                    val strokeWidth = 4.dp

                    // Superior izquierda
                    Column(modifier = Modifier.align(Alignment.TopStart)) {
                        Box(modifier = Modifier.width(cornerSize).height(strokeWidth).background(cornerColor))
                        Box(modifier = Modifier.width(strokeWidth).height(cornerSize).background(cornerColor))
                    }
                    // Superior derecha
                    Column(modifier = Modifier.align(Alignment.TopEnd)) {
                        Box(modifier = Modifier.width(cornerSize).height(strokeWidth).background(cornerColor))
                        Box(modifier = Modifier.width(strokeWidth).height(cornerSize).background(cornerColor).align(Alignment.End))
                    }
                    // Inferior izquierda
                    Column(modifier = Modifier.align(Alignment.BottomStart)) {
                        Box(modifier = Modifier.width(strokeWidth).height(cornerSize).background(cornerColor))
                        Box(modifier = Modifier.width(cornerSize).height(strokeWidth).background(cornerColor))
                    }
                    // Inferior derecha
                    Column(modifier = Modifier.align(Alignment.BottomEnd)) {
                        Box(modifier = Modifier.width(strokeWidth).height(cornerSize).background(cornerColor).align(Alignment.End))
                        Box(modifier = Modifier.width(cornerSize).height(strokeWidth).background(cornerColor))
                    }
                }
            }
        }
    }

    // ── Pantalla principal ───────────────────────────────────────────────────
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(LightGrayBg)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(top = 20.dp, bottom = 40.dp)
    ) {
        // ── Barra superior ─────────────────────────────────────────────────
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

        // ── Cabecera ────────────────────────────────────────────────────────
        item {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "PORTAL DE ACCESO", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextGray, letterSpacing = 1.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = "Muestra este código al\nadministrador", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = NavyBlue, textAlign = TextAlign.Center, lineHeight = 32.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Box(modifier = Modifier.width(40.dp).height(3.dp).clip(CircleShape).background(Color(0xFFD1D5DB)))
            }
            Spacer(modifier = Modifier.height(30.dp))
        }

        // ── Tarjeta QR ──────────────────────────────────────────────────────
        item {
            Card(
                modifier = Modifier.fillMaxWidth().shadow(elevation = 12.dp, shape = RoundedCornerShape(32.dp)),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier.fillMaxWidth().aspectRatio(1f).clip(RoundedCornerShape(24.dp)).background(InputBg).padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        qrBitmap?.let {
                            Image(bitmap = it.asImageBitmap(), contentDescription = "QR", modifier = Modifier.fillMaxSize())
                        } ?: CircularProgressIndicator(color = NavyBlue)
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(text = userData?.u ?: "Usuario", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1F2937))
                    Text(text = "C.I: V-${userData?.c ?: ""}", fontSize = 14.sp, color = TextGray)

                    Spacer(modifier = Modifier.height(16.dp))

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
                                Text(
                                    text = decryptedId ?: "",
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = NavyBlue
                                )
                            }
                            LinearProgressIndicator(
                                progress = idProgress,
                                modifier = Modifier.width(100.dp).height(4.dp).clip(CircleShape),
                                color = NavyBlue,
                                trackColor = Color(0xFFE2E8F0)
                            )
                        }
                    }

                    TextButton(
                        onClick = { userData?.let { viewModel.showDecryptedId(it.aid) } }
                    ) {
                        Icon(
                            imageVector = if (decryptedId == null) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = NavyBlue
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (decryptedId == null) "VER ID TÉCNICO" else "OCULTAR ID",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = NavyBlue
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    StatusBadge(
                        text = if (isActivated == true) "ACCESO ACTIVADO" else "ESPERANDO VALIDACIÓN",
                        color = if (isActivated == true) Color(0xFF00C853) else NavyBlue,
                        pulse = isActivated != true
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // ── Panel de resultado del escaneo QR ────────────────────────────────
        item {
            AnimatedVisibility(
                visible = provMessage != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = when (provSuccess) {
                            true  -> Color(0xFFF0FDF4)  // verde muy claro
                            false -> Color(0xFFFFF1F2)  // rojo muy claro
                            null  -> Color(0xFFF8FAFC)
                        }
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        when (provSuccess) {
                            true  -> Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF16A34A),
                                modifier = Modifier.size(22.dp)
                            )
                            false -> Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color(0xFFDC2626),
                                modifier = Modifier.size(22.dp)
                            )
                            null  -> {}
                        }
                        Column {
                            Text(
                                text = provMessage ?: "",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = when (provSuccess) {
                                    true  -> Color(0xFF16A34A)
                                    false -> Color(0xFFDC2626)
                                    null  -> Color(0xFF6B7280)
                                }
                            )
                        }
                    }
                }
            }
        }

        // ── Botón principal: Escanear QR del Admin ───────────────────────────
        item {
            Button(
                onClick = {
                    if (cameraPermission.status.isGranted) {
                        viewModel.resetProvisioningState()
                        showScanner = true
                    } else {
                        cameraPermission.launchPermissionRequest()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(64.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isActivated == true) Color(0xFF16A34A) else NavyBlue
                ),
                enabled = isActivated != true
            ) {
                Icon(
                    imageVector = if (isActivated == true) Icons.Default.Check else Icons.Default.QrCodeScanner,
                    contentDescription = null,
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = if (isActivated == true) "ACTIVADO" else "Escanear QR del Admin",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ── Botón secundario: verificación manual por endpoint ──────────
            OutlinedButton(
                onClick = {
                    validationError = null
                    viewModel.validateOnEndpoint(
                        cedula = userData?.c ?: "",
                        onSuccess = { onActivated() },
                        onError = { validationError = it }
                    )
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = NavyBlue),
                enabled = !isValidating && isActivated != true
            ) {
                if (isValidating) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = NavyBlue, strokeWidth = 2.dp)
                } else {
                    Icon(imageVector = Icons.Default.Sync, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Verificar por servidor", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            AnimatedVisibility(visible = validationError != null) {
                Text(text = validationError ?: "", color = Color.Red, fontSize = 13.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 12.dp))
            }

            TextButton(onClick = { onReset() }, modifier = Modifier.padding(top = 8.dp)) {
                Text("Volver a editar perfil", color = TextGray)
            }
        }
    }
}
