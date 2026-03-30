package com.example.qr_prueba_gaby.presentation.ui.MainScreen.Components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.qr_prueba_gaby.presentation.ui.theme.MainBlue
import com.example.qr_prueba_gaby.presentation.ui.theme.TextGray

@Composable
fun WelcomeHeader(userName: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "BIENVENIDO DE NUEVO",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = TextGray,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = userName,
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MainBlue,
            textAlign = TextAlign.Center
        )
    }
}
