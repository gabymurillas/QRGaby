package com.example.qr_prueba_gaby.presentation.ui.RegistrationScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.qr_prueba_gaby.presentation.ui.RegistrationScreen.Components.*
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
        item {
            RegistrationTopBar()
            Spacer(modifier = Modifier.height(40.dp))
        }

        item {
            RegistrationTitles()
            Spacer(modifier = Modifier.height(32.dp))
        }

        item {
            RegistrationForm(
                nombre = nombre,
                cedula = cedula,
                plates = plates,
                isGenerating = isGenerating,
                error = error,
                viewModel = viewModel,
                onNavigateToQr = onNavigateToQr
            )
        }
    }
}
