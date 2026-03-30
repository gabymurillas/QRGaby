package com.example.qr_prueba_gaby.presentation.ui.MainScreen.Components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.qr_prueba_gaby.presentation.ui.theme.MainBlue
import com.example.qr_prueba_gaby.presentation.ui.theme.TextGray

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SystemStatusCard() {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth().height(90.dp),
        shape = RoundedCornerShape(24.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    color = MainBlue
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "ESTADO DEL SISTEMA",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextGray
                    )
                    Text(
                        text = "Activo",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MainBlue
                    )
                }
            }
            Badge(
                modifier = Modifier.align(Alignment.TopEnd).padding(top = 16.dp, end = 16.dp),
                containerColor = Color(0xFFE0E7FF),
                contentColor = Color(0xFF4338CA)
            ) {
                Text(
                    text = "VERIFICADO",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}
