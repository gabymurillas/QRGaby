package com.example.qr_prueba_gaby.presentation.ui.MainScreen.Components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.qr_prueba_gaby.presentation.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun PinEntryDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    error: String? = null
) {
    var pin by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(28.dp),
            color = SurfaceDark
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Seguridad",
                    color = TextGray,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Ingrese PIN de Acceso",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold
                )
                
                Spacer(modifier = Modifier.height(24.dp))

                // Indicadores de PIN (Dots)
                PinDotsIndicator(pinLength = pin.length)

                if (error != null) {
                    Text(
                        text = error,
                        color = RedAccent,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Teclado Numérico Personalizado
                NumericKeypad(
                    onNumberClick = { if (pin.length < 4) pin += it },
                    onDeleteClick = { if (pin.isNotEmpty()) pin = pin.dropLast(1) }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Botones de acción
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar", color = TextSecondary)
                    }
                    Button(
                        onClick = { onConfirm(pin) },
                        enabled = pin.length == 4,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MainBlue,
                            disabledContainerColor = MainBlue.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier.height(45.dp).padding(horizontal = 8.dp)
                    ) {
                        Text("Confirmar", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun PinSetupDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var pin by remember { mutableStateOf("") }
    var step by remember { mutableIntStateOf(1) }
    var firstPin by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.85f).wrapContentHeight(),
            shape = RoundedCornerShape(28.dp),
            color = SurfaceDark
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (step == 1) "Configuración" else "Confirmación",
                    color = GreenAccent,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (step == 1) "Cree su PIN de Seguridad" else "Repita su PIN",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
                
                Spacer(modifier = Modifier.height(24.dp))

                PinDotsIndicator(pinLength = pin.length)

                errorMsg?.let {
                    Text(text = it, color = RedAccent, fontSize = 12.sp, modifier = Modifier.padding(top = 12.dp))
                }

                Spacer(modifier = Modifier.height(32.dp))

                NumericKeypad(
                    onNumberClick = { 
                        if (pin.length < 4) {
                            pin += it
                            errorMsg = null
                            if (pin.length == 4) {
                                // Pequeña espera para que se vea el último punto lleno
                            }
                        }
                    },
                    onDeleteClick = { if (pin.isNotEmpty()) pin = pin.dropLast(1) }
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    TextButton(onClick = onDismiss) { Text("Cancelar", color = TextSecondary) }
                    Button(
                        onClick = { 
                            if (step == 1) {
                                firstPin = pin
                                pin = ""
                                step = 2
                            } else {
                                if (pin == firstPin) {
                                    onConfirm(pin)
                                } else {
                                    errorMsg = "Los PIN no coinciden"
                                    pin = ""
                                    step = 1
                                }
                            }
                        },
                        enabled = pin.length == 4,
                        colors = ButtonDefaults.buttonColors(containerColor = GreenAccent)
                    ) {
                        Text(if (step == 1) "Siguiente" else "Guardar", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun PinDotsIndicator(pinLength: Int) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(4) { index ->
            val isFilled = index < pinLength
            val color by animateColorAsState(
                targetValue = if (isFilled) MainBlue else Color.White.copy(alpha = 0.2f),
                label = ""
            )
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}

@Composable
fun NumericKeypad(
    onNumberClick: (String) -> Unit,
    onDeleteClick: () -> Unit
) {
    val numbers = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("", "0", "DEL")
    )

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        numbers.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                row.forEach { char ->
                    if (char.isEmpty()) {
                        Spacer(modifier = Modifier.size(64.dp))
                    } else {
                        KeypadButton(
                            text = char,
                            onClick = { if (char == "DEL") onDeleteClick() else onNumberClick(char) },
                            isDelete = char == "DEL"
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun KeypadButton(
    text: String,
    onClick: () -> Unit,
    isDelete: Boolean = false
) {
    Surface(
        modifier = Modifier
            .size(64.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        color = Color.White.copy(alpha = 0.05f),
        contentColor = Color.White,
        shape = CircleShape
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (isDelete) {
                Icon(Icons.Default.Backspace, contentDescription = null, modifier = Modifier.size(24.dp), tint = TextSecondary)
            } else {
                Text(text = text, fontSize = 24.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}
