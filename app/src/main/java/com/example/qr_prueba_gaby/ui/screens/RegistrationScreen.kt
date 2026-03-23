package com.example.qr_prueba_gaby.ui.screens

import android.content.Context
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.qr_prueba_gaby.ui.viewmodel.AppViewModel

@Composable
fun RegistrationScreen(viewModel: AppViewModel, context: Context, onNavigateToQr: () -> Unit) {
    val nombre by viewModel.nombre.collectAsState()
    val cedula by viewModel.cedula.collectAsState()
    val plates by viewModel.plates.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()
    val error by viewModel.registrationError.collectAsState()

    val bgGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF0D1117), Color(0xFF0A1628))
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(bgGradient)
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 40.dp)
    ) {
        // ── Header ──
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xFF1565C0).copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = null,
                        tint = Color(0xFF42A5F5),
                        modifier = Modifier.size(44.dp)
                    )
                }
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "Mi Acceso Residencial",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Registra tus datos para obtener tu llave digital",
                    fontSize = 14.sp,
                    color = Color(0xFF8899AA),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }

        // ── Card: Datos personales ──
        item {
            SectionCard(title = "Datos Personales", icon = Icons.Default.Person) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    AppTextField(
                        value = nombre,
                        onValueChange = { viewModel.onNombreChange(it) },
                        label = "Nombre y Apellido",
                        placeholder = "Ej: Juan Pérez",
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words
                        )
                    )
                    AppTextField(
                        value = cedula,
                        onValueChange = { viewModel.onCedulaChange(it) },
                        label = "Cédula de Identidad",
                        placeholder = "Solo números",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            }
        }

        // ── Card: Vehículos ──
        item {
            SectionCard(title = "Mis Vehículos", icon = Icons.Default.DirectionsCar) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    plates.forEachIndexed { index, plate ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AppTextField(
                                value = plate,
                                onValueChange = { viewModel.onPlateChange(index, it) },
                                label = "Placa ${index + 1}",
                                placeholder = "Ej: ABC-123",
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(
                                    capitalization = KeyboardCapitalization.Characters
                                )
                            )
                            if (index > 0) {
                                IconButton(
                                    onClick = { viewModel.removePlate(index) },
                                    modifier = Modifier.size(44.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Eliminar placa",
                                        tint = Color(0xFFEF5350)
                                    )
                                }
                            }
                        }
                    }

                    if (plates.size < 5) {
                        TextButton(
                            onClick = { viewModel.addPlate() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = null,
                                tint = Color(0xFF42A5F5)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text("Agregar vehículo", color = Color(0xFF42A5F5))
                        }
                    }
                }
            }
        }

        // ── Error ──
        item {
            AnimatedVisibility(visible = error != null) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFEF5350).copy(alpha = 0.15f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color(0xFFEF5350),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(error ?: "", color = Color(0xFFEF5350), fontSize = 14.sp)
                    }
                }
            }
        }

        // ── Botón generar llave ──
        item {
            Button(
                onClick = {
                    viewModel.generateKey(
                        onSuccess = onNavigateToQr
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1565C0),
                    disabledContainerColor = Color(0xFF1565C0).copy(alpha = 0.5f)
                ),
                enabled = !isGenerating
            ) {
                if (isGenerating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(10.dp))
                    Text("Generando llave...", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                } else {
                    Icon(Icons.Default.Lock, contentDescription = null)
                    Spacer(Modifier.width(10.dp))
                    Text("Generar Mi Llave", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF111827))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(icon, contentDescription = null, tint = Color(0xFF42A5F5), modifier = Modifier.size(20.dp))
                Text(title, fontWeight = FontWeight.SemiBold, color = Color.White, fontSize = 16.sp)
            }
            content()
        }
    }
}

@Composable
private fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = Color(0xFF8899AA)) },
        placeholder = { Text(placeholder, color = Color(0xFF445566)) },
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        keyboardOptions = keyboardOptions,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedBorderColor = Color(0xFF42A5F5),
            unfocusedBorderColor = Color(0xFF223344),
            cursorColor = Color(0xFF42A5F5)
        )
    )
}
