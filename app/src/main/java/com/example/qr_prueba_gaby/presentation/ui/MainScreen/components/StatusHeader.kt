package com.example.qr_prueba_gaby.presentation.ui.MainScreen.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.qr_prueba_gaby.presentation.ui.theme.textSecondary
import com.example.qr_prueba_gaby.presentation.ui.common.OdooStatus

@Composable
fun StatusHeader(
    odooStatus: OdooStatus,
    onSettingsClick: () -> Unit
) {
    val statusColor by animateColorAsState(
        targetValue = when (odooStatus) {
            OdooStatus.VALID -> Color(0xFF00C853)
            OdooStatus.INVALID -> Color.Red
            OdooStatus.VERIFYING -> Color.Yellow
            else -> Color.Gray
        },
        animationSpec = tween(durationMillis = 500),
        label = "statusColorAnimation"
    )

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(statusColor)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = when (odooStatus) {
                    OdooStatus.VALID     -> "Sincronizado"
                    OdooStatus.VERIFYING -> "Sincronizando..."
                    OdooStatus.INVALID   -> "Usuario no encontrado"
                    OdooStatus.OFFLINE   -> "Sin conexión"
                },
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.textSecondary
            )
        }

        Spacer(Modifier.weight(1f))

        IconButton(onClick = onSettingsClick) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Ajustes",
                tint = MaterialTheme.colorScheme.textSecondary
            )
        }
    }
}
