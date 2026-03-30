package com.example.qr_prueba_gaby.presentation.ui.RegistrationScreen.Components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.qr_prueba_gaby.presentation.ui.theme.MainBlue
import com.example.qr_prueba_gaby.presentation.ui.theme.TextGray

@Composable
fun RegistrationTitles() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Registra tu Perfil",
            fontSize = 32.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MainBlue
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Ingresa tus datos completos para autorizar el acceso.",
            fontSize = 15.sp,
            color = TextGray
        )
    }
}
