package com.example.qr_prueba_gaby.presentation.ui.screens

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.qr_prueba_gaby.presentation.ui.viewmodels.AppViewModel
import com.example.qr_prueba_gaby.presentation.ui.theme.*

@Composable
fun RegistrationScreen(viewModel: AppViewModel, context: Context, onNavigateToQr: () -> Unit) {
    val nombre by viewModel.nombre.collectAsState()
    val cedula by viewModel.cedula.collectAsState()
    val plates by viewModel.plates.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()
    val error by viewModel.registrationError.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(LightGrayBg)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(top = 40.dp, bottom = 40.dp)
    ) {
        // ── Top Bar ──
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "Logo",
                        tint = MainBlue,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "EscanQR",
                        color = MainBlue,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black
                    )
                }
                Icon(
                    imageVector = Icons.Default.Help,
                    contentDescription = "Ayuda",
                    tint = MainBlue
                )
            }
            Spacer(modifier = Modifier.height(40.dp))
        }

        // ── Titles ──
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Registra tu Perfil",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MainBlue
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Ingresa tus siguientes credenciales\npara autorizarte el acceso al sistema.",
                    fontSize = 15.sp,
                    color = TextGray,
                    lineHeight = 22.sp
                )
            }
            Spacer(modifier = Modifier.height(32.dp))
        }

        // ── Main Card Form ──
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    
                    OutlinedTextField(
                        value = nombre,
                        onValueChange = { viewModel.onNombreChange(it) },
                        label = { Text("Nombre de Usuario") },
                        placeholder = { Text("Ej. Juan Perez") },
                        leadingIcon = { Icon(Icons.Outlined.PersonOutline, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            focusedBorderColor = MainBlue,
                            unfocusedBorderColor = Color.Gray,
                            focusedLabelColor = MainBlue,
                            unfocusedLabelColor = Color.Gray,
                            cursorColor = MainBlue
                        )
                    )
                    
                    OutlinedTextField(
                        value = cedula,
                        onValueChange = { viewModel.onCedulaChange(it) },
                        label = { Text("Cédula de Identidad") },
                        placeholder = { Text("Ej. 12345678") },
                        leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            focusedBorderColor = MainBlue,
                            unfocusedBorderColor = Color.Gray,
                            focusedLabelColor = MainBlue,
                            unfocusedLabelColor = Color.Gray,
                            cursorColor = MainBlue
                        )
                    )

                    // Plates
                    plates.forEachIndexed { index, plate ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = plate,
                                onValueChange = { viewModel.onPlateChange(index, it) },
                                label = { Text(if (index == 0) "Placa del Vehículo" else "Placa Adicional ${index + 1}") },
                                placeholder = { Text("Ej. ABC-1234") },
                                leadingIcon = { Icon(Icons.Default.DirectionsCar, contentDescription = null) },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.Black,
                                    unfocusedTextColor = Color.Black,
                                    focusedBorderColor = MainBlue,
                                    unfocusedBorderColor = Color.Gray,
                                    focusedLabelColor = MainBlue,
                                    unfocusedLabelColor = Color.Gray,
                                    cursorColor = MainBlue
                                )
                            )
                            if (index > 0) {
                                IconButton(onClick = { viewModel.removePlate(index) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color.Red.copy(alpha = 0.7f))
                                }
                            }
                        }
                    }

                    if (plates.size < 5) {
                        TextButton(
                            onClick = { viewModel.addPlate() },
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Añadir otro vehículo")
                        }
                    }

                    // ── Error ──
                    AnimatedVisibility(visible = error != null) {
                        Text(
                            text = error ?: "", 
                            color = MaterialTheme.colorScheme.error, 
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    // ── Botón nativo M3 ──
                    Button(
                        onClick = { viewModel.generateKey(onSuccess = onNavigateToQr) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MainBlue),
                        enabled = !isGenerating
                    ) {
                        if (isGenerating) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                        } else {
                            Text("REGISTRARSE", fontWeight = FontWeight.Bold)
                            Spacer(Modifier.width(12.dp))
                            Icon(Icons.Default.ArrowForward, contentDescription = null)
                        }
                    }
                }
            }
        }
    }
}
