package com.example.qr_prueba_gaby

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.qr_prueba_gaby.ui.theme.QRPRUEBAGABYTheme
import com.google.zxing.WriterException
import java.security.MessageDigest

private const val SECRET_KEY = "TuClaveSecretaAqui"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            QRPRUEBAGABYTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    QRGeneratorScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun QRGeneratorScreen(
    modifier: Modifier = Modifier,
    viewModel: QRGeneratorViewModel = viewModel()
) {
    val context = LocalContext.current
    
    val androidId = remember {
        Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID) ?: "N/A"
    }

    val qrBitmap by viewModel.qrBitmap.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    LaunchedEffect(androidId) {
        viewModel.generateQR(androidId)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Mi ID Seguro (MVVM)",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(32.dp))

        Box(
            modifier = Modifier.size(300.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator()
            } else if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                qrBitmap?.let {
                    Image(
                        bitmap = it.asImageBitmap(),
                        contentDescription = "QR",
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = { viewModel.generateQR(androidId) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("REGENERAR QR (Nuevo Token)")
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text("ID Visible:", fontWeight = FontWeight.Bold)
        Text(androidId, fontSize = 16.sp)

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                val sendIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, "ID: $androidId")
                    type = "text/plain"
                }
                context.startActivity(Intent.createChooser(sendIntent, "Compartir"))
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("COMPARTIR TEXTO")
        }
    }
}

