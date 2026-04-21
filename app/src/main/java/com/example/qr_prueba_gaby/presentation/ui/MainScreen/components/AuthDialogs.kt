package com.example.qr_prueba_gaby.presentation.ui.MainScreen.components

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
fun PinEntryDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit, error: String? = null) {
    var pin by remember { mutableStateOf("") }
    LaunchedEffect(pin) {
        if (pin.length == 4) {
            delay(200)
            onConfirm(pin)
        }
    }
    BasePinDialog(
        title = "Ingrese PIN de Acceso",
        subtitle = "Seguridad",
        pin = pin,
        onPinChange = { if (it.length <= 4) pin = it },
        onDismiss = onDismiss,
        error = error
    )
}

@Composable
fun PinSetupDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var pin by remember { mutableStateOf("") }
    var step by remember { mutableIntStateOf(1) }
    var firstPin by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(pin) {
        if (pin.length == 4) {
            delay(250)
            if (step == 1) {
                firstPin = pin
                pin = ""
                step = 2
            } else {
                if (pin == firstPin) onConfirm(pin)
                else {
                    errorMsg = "Los PIN no coinciden"
                    pin = ""
                    step = 1
                }
            }
        }
    }

    BasePinDialog(
        title = if (step == 1) "Cree su PIN" else "Repita su PIN",
        subtitle = if (step == 1) "Configuración" else "Confirmación",
        pin = pin,
        onPinChange = { pin = it; errorMsg = null },
        onDismiss = onDismiss,
        error = errorMsg
    )
}

@Composable
private fun BasePinDialog(
    title: String,
    subtitle: String,
    pin: String,
    onPinChange: (String) -> Unit,
    onDismiss: () -> Unit,
    error: String?
) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.85f).wrapContentHeight(),
            shape = RoundedCornerShape(28.dp),
            color = SurfaceDark
        ) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = subtitle, color = TextGray, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text(text = title, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(24.dp))
                PinDotsIndicator(pinLength = pin.length, isError = error != null)
                error?.let { Text(it, color = RedAccent, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp)) }
                Spacer(modifier = Modifier.height(32.dp))
                NumericKeypad(
                    onNumberClick = { if (pin.length < 4) onPinChange(pin + it) },
                    onDeleteClick = { if (pin.isNotEmpty()) onPinChange(pin.dropLast(1)) }
                )
                TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.Start)) {
                    Text("Cancelar", color = TextSecondary)
                }
            }
        }
    }
}

@Composable
fun PinDotsIndicator(pinLength: Int, isError: Boolean) {
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        repeat(4) { index ->
            val isFilled = index < pinLength
            val color by animateColorAsState(
                targetValue = if (isError) RedAccent else if (isFilled) MainBlue else Color.White.copy(alpha = 0.2f),
                label = ""
            )
            Box(modifier = Modifier.size(16.dp).clip(CircleShape).background(color))
        }
    }
}

@Composable
fun NumericKeypad(onNumberClick: (String) -> Unit, onDeleteClick: () -> Unit) {
    val rows = listOf(listOf("1","2","3"), listOf("4","5","6"), listOf("7","8","9"), listOf("","0","DEL"))
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        rows.forEach { row ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                row.forEach { char ->
                    if (char.isEmpty()) Spacer(modifier = Modifier.size(64.dp))
                    else KeypadButton(text = char, onClick = { if (char == "DEL") onDeleteClick() else onNumberClick(char) }, isDelete = char == "DEL")
                }
            }
        }
    }
}

@Composable
fun KeypadButton(text: String, onClick: () -> Unit, isDelete: Boolean) {
    Surface(
        modifier = Modifier.size(64.dp).clip(CircleShape).clickable { onClick() },
        color = Color.White.copy(alpha = 0.05f),
        contentColor = Color.White
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (isDelete) Icon(Icons.Default.Backspace, null, modifier = Modifier.size(24.dp), tint = TextGray)
            else Text(text, fontSize = 24.sp, fontWeight = FontWeight.Medium)
        }
    }
}
