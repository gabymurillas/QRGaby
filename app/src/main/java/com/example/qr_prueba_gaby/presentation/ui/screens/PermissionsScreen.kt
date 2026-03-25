package com.example.qr_prueba_gaby.presentation.ui.screens

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

/**
 * Pantalla de guardián que solicita los permisos de Bluetooth y Ubicación
 * antes de mostrar el panel principal. Si los permisos ya están otorgados,
 * navega automáticamente.
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionsScreen(onPermissionsGranted: () -> Unit) {

    val blePermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        listOf(
            android.Manifest.permission.BLUETOOTH_SCAN,
            android.Manifest.permission.BLUETOOTH_CONNECT
        )
    } else {
        listOf(
            android.Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    val permissionsState = rememberMultiplePermissionsState(blePermissions)

    LaunchedEffect(permissionsState.allPermissionsGranted) {
        if (permissionsState.allPermissionsGranted) {
            onPermissionsGranted()
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
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Bluetooth,
                contentDescription = null,
                tint = Color(0xFF42A5F5),
                modifier = Modifier.size(64.dp)
            )

            Text(
                text = "Permiso de Bluetooth",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Text(
                text = "Para detectar y abrir el portón, esta app necesita acceso a Bluetooth.",
                fontSize = 14.sp,
                color = Color(0xFF8899AA),
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )

            Button(
                onClick = { permissionsState.launchMultiplePermissionRequest() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0))
            ) {
                Text("Permitir Bluetooth", fontWeight = FontWeight.SemiBold)
            }

            if (permissionsState.shouldShowRationale) {
                Text(
                    text = "Sin este permiso, el botón del portón no funcionará.",
                    fontSize = 12.sp,
                    color = Color(0xFFEF5350),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
