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
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.qr_prueba_gaby.presentation.ui.viewmodels.RegistrationViewModel
import com.example.qr_prueba_gaby.presentation.ui.theme.*

@Composable
fun RegistrationScreen(
    viewModel: RegistrationViewModel, 
    onNavigateToQr: () -> Unit
) {
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
                    Icon(Icons.Default.Security, contentDescription = null, tint = MainBlue, modifier = Modifier.size(28.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "EscanQR", color = MainBlue, fontSize = 18.sp, fontWeight = FontWeight.Black)
                }
            }
            Spacer(modifier = Modifier.height(40.dp))
        }

        // ── Titles ──
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(text = "Registra tu Perfil", fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = MainBlue)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Ingresa tus datos completos para autorizar el acceso.", fontSize = 15.sp, color = TextGray)
            }
            Spacer(modifier = Modifier.height(32.dp))
        }

        // ── Card Form ──
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    
                    OutlinedTextField(
                        value = nombre,
                        onValueChange = { viewModel.onNombreChange(it) },
                        label = { Text("Nombre y Apellido") },
                        leadingIcon = { Icon(Icons.Outlined.PersonOutline, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        isError = error?.contains("Nombre") == true,
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
                        leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        isError = error?.contains("Cédula") == true,
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

                    plates.forEachIndexed { index, plate ->
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = plate,
                                onValueChange = { viewModel.onPlateChange(index, it) },
                                label = { Text("Placa del Vehículo") },
                                leadingIcon = { Icon(Icons.Default.DirectionsCar, contentDescription = null) },
                                modifier = Modifier.weight(1f),
                                isError = error?.contains("placas") == true,
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
                                    Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red.copy(0.7f))
                                }
                            }
                        }
                    }

                    if (plates.size < 5) {
                        TextButton(onClick = { viewModel.addPlate() }) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Añadir otro vehículo")
                        }
                    }

                    AnimatedVisibility(visible = error != null) {
                        Text(text = error ?: "", color = Color.Red, fontSize = 13.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                    }

                    Button(
                        onClick = { viewModel.register(onSuccess = onNavigateToQr) },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MainBlue),
                        enabled = !isGenerating,
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        if (isGenerating) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        else Text("CONTINUAR", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
