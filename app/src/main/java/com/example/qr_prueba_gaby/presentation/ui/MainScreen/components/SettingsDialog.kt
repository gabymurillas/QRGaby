package com.example.qr_prueba_gaby.presentation.ui.MainScreen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.qr_prueba_gaby.data.model.ThemeMode
import com.example.qr_prueba_gaby.presentation.ui.theme.*

/**
 * Modal de ajustes de la app.
 *
 * Reúne dos ajustes:
 *  - **PIN de seguridad** (opcional): el usuario decide si lo activa y su valor.
 *  - **Tema de la app**: claro, oscuro o seguir al sistema.
 *
 * @param hasPin            true si actualmente hay un PIN configurado.
 * @param onTogglePin       se llama al cambiar el interruptor del PIN (true = activar).
 * @param onChangePin       se llama para cambiar el PIN actual.
 * @param themeMode         modo de tema actualmente seleccionado.
 * @param onThemeModeChange se llama al elegir un nuevo modo de tema.
 */
@Composable
fun SettingsDialog(
    hasPin: Boolean,
    onTogglePin: (Boolean) -> Unit,
    onChangePin: () -> Unit,
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
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

                // ── Apariencia ───────────────────────────────────────────────
                Spacer(modifier = Modifier.height(20.dp))
                HorizontalDivider(color = SurfaceAlt)
                Spacer(modifier = Modifier.height(20.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.DarkMode, contentDescription = null, tint = MainBlue)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Tema de la app", color = Color.White, fontWeight = FontWeight.SemiBold)
                        Text(
                            text = "Elige claro, oscuro o seguir el sistema",
                            color = TextGray,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                ThemeModeSelector(selected = themeMode, onSelect = onThemeModeChange)

                Spacer(modifier = Modifier.height(16.dp))
                TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) {
                    Text("Cerrar", color = TextSecondary)
                }
            }
        }
    }
}

/**
 * Selector segmentado de tres opciones para el modo de tema.
 *
 * Usa colores explícitos (no tokens) porque vive dentro de un diálogo de
 * superficie oscura fija, que es igual en tema claro y oscuro.
 */
@Composable
private fun ThemeModeSelector(
    selected: ThemeMode,
    onSelect: (ThemeMode) -> Unit
) {
    val options = listOf(
        ThemeMode.SYSTEM to "Sistema",
        ThemeMode.LIGHT  to "Claro",
        ThemeMode.DARK   to "Oscuro"
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceAlt)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        options.forEach { (mode, label) ->
            val isSelected = mode == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isSelected) MainBlue else Color.Transparent)
                    .clickable { onSelect(mode) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    color = if (isSelected) Color.White else TextGray,
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                )
            }
        }
    }
}
