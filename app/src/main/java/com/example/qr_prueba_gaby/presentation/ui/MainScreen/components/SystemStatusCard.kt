package com.example.qr_prueba_gaby.presentation.ui.MainScreen.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.qr_prueba_gaby.presentation.ui.common.OdooStatus
import com.example.qr_prueba_gaby.presentation.ui.theme.MainBlue
import com.example.qr_prueba_gaby.presentation.ui.theme.TextGray

/**
 * Tarjeta de estado del sistema. Refleja el mismo [OdooStatus] que el
 * indicador de sincronización del encabezado, para que ambos "hablen el
 * mismo idioma".
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SystemStatusCard(odooStatus: OdooStatus) {
    val accent = when (odooStatus) {
        OdooStatus.VALID     -> MainBlue
        OdooStatus.VERIFYING -> Color(0xFFF59E0B)
        OdooStatus.INVALID   -> Color(0xFFDC2626)
        OdooStatus.OFFLINE   -> Color(0xFF6B7280)
    }
    val title = when (odooStatus) {
        OdooStatus.VALID     -> "Activo"
        OdooStatus.VERIFYING -> "Verificando..."
        OdooStatus.INVALID   -> "No encontrado"
        OdooStatus.OFFLINE   -> "Sin conexión"
    }
    val badgeText = when (odooStatus) {
        OdooStatus.VALID     -> "VERIFICADO"
        OdooStatus.VERIFYING -> "VERIFICANDO"
        OdooStatus.INVALID   -> "NO VERIFICADO"
        OdooStatus.OFFLINE   -> "SIN CONEXIÓN"
    }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth().height(90.dp),
        shape = RoundedCornerShape(24.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    color = accent
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "ESTADO DEL SISTEMA",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextGray
                    )
                    Text(
                        text = title,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = accent,
                        maxLines = 1
                    )
                }
            }
            Badge(
                modifier = Modifier.align(Alignment.TopEnd).padding(top = 16.dp, end = 16.dp),
                containerColor = accent.copy(alpha = 0.15f),
                contentColor = accent
            ) {
                Text(
                    text = badgeText,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}
