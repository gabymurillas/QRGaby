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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.qr_prueba_gaby.presentation.ui.QrSyncScreen.Components.*
import kotlinx.coroutines.delay

@Composable
fun QrSyncScreen(
    viewModel: SyncViewModel,
    onActivated: () -> Unit,
    onReset: () -> Unit
) {
    val qrBitmap by viewModel.qrBitmap.collectAsState()
    val isActivated by viewModel.isActivatedFlow.collectAsStateWithLifecycle(null)
    val userData by viewModel.userDataFlow.collectAsStateWithLifecycle(null)
    val isValidating by viewModel.isValidating.collectAsStateWithLifecycle(false)
    
    val decryptedId by viewModel.decryptedAndroidId.collectAsStateWithLifecycle()
    val idProgress by viewModel.idVisibilityProgress.collectAsStateWithLifecycle()
    
    var validationError by remember { mutableStateOf<String?>(null) }

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
            Spacer(modifier = Modifier.height(40.dp))
        }

        item {
            Button(
                onClick = {
                    validationError = null
                    viewModel.validateOnEndpoint(
                        cedula = userData?.c ?: "",
                        onSuccess = { onActivated() },
                        onError = { validationError = it }
                    )
                },
                modifier = Modifier.fillMaxWidth().height(64.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NavyBlue),
                enabled = !isValidating && isActivated != true
            ) {
                if (isValidating) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                } else {
                    Icon(imageVector = if (isActivated == true) Icons.Default.Check else Icons.Default.Sync, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = if (isActivated == true) "ACTIVADO" else "Verificar", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
            
            AnimatedVisibility(visible = validationError != null) {
                Text(text = validationError ?: "", color = Color.Red, fontSize = 13.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 12.dp))
            }
            
            TextButton(onClick = { onReset() }, modifier = Modifier.padding(top = 16.dp)) {
                Text("Volver a editar perfil", color = TextGray)
            }
        }
    }
}
