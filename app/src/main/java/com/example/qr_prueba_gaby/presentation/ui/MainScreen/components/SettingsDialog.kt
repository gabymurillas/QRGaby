package com.example.qr_prueba_gaby.presentation.ui.MainScreen.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.qr_prueba_gaby.presentation.ui.theme.*

/**
 * Modal de ajustes de seguridad.
 *
 * El PIN de seguridad es OPCIONAL: el usuario decide si lo activa y cuál es su
 * valor. Desde aquí puede activarlo/desactivarlo y cambiarlo.
 *
 * @param hasPin      true si actualmente hay un PIN configurado.
 * @param onTogglePin se llama al cambiar el interruptor (true = activar).
 * @param onChangePin se llama para cambiar el PIN actual.
 */
@Composable
fun SettingsDialog(
    hasPin: Boolean,
    onTogglePin: (Boolean) -> Unit,
    onChangePin: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            color = SurfaceDark
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Seguridad", color = TextGray, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text("Ajustes", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)

                Spacer(modifier = Modifier.height(20.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = MainBlue)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("PIN de seguridad", color = Color.White, fontWeight = FontWeight.SemiBold)
                        Text(
                            text = if (hasPin) "Activado" else "Desactivado",
                            color = TextGray,
                            fontSize = 12.sp
                        )
                    }
                    Switch(checked = hasPin, onCheckedChange = onTogglePin)
                }

                if (hasPin) {
                    TextButton(onClick = onChangePin) {
                        Text("Cambiar PIN", color = MainBlue)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "El PIN es opcional. Si lo desactivas, el portón se abrirá " +
                        "con tu huella; si tu teléfono no tiene huella, se abrirá directo.",
                    color = TextGray,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(16.dp))
                TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) {
                    Text("Cerrar", color = TextSecondary)
                }
            }
        }
    }
}
