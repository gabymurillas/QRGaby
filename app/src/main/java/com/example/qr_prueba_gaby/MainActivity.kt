package com.example.qr_prueba_gaby

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.qr_prueba_gaby.ui.screens.MainScreen
import com.example.qr_prueba_gaby.ui.screens.PermissionsScreen
import com.example.qr_prueba_gaby.ui.screens.QrSyncScreen
import com.example.qr_prueba_gaby.ui.screens.RegistrationScreen
import com.example.qr_prueba_gaby.ui.theme.QRPRUEBAGABYTheme
import com.example.qr_prueba_gaby.ui.viewmodel.AppViewModel
import dagger.hilt.android.AndroidEntryPoint

// Rutas de navegación
private const val ROUTE_REGISTRATION  = "registration"
private const val ROUTE_QR_SYNC       = "qr_sync"
private const val ROUTE_PERMISSIONS   = "permissions"
private const val ROUTE_MAIN          = "main"

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            QRPRUEBAGABYTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF0D1117)
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
private fun AppNavigation() {
    val context = LocalContext.current
    val viewModel: AppViewModel = hiltViewModel()
    val navController = rememberNavController()

    // Usamos null como valor inicial para saber si todavía estamos leyendo de DataStore
    val isRegistered by viewModel.isRegisteredFlow.collectAsStateWithLifecycle()

    // Pantalla de carga mientras se determina el destino inicial
    if (isRegistered == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Color.Cyan)
        }
        return
    }

    val startDestination = if (isRegistered == true) ROUTE_MAIN else ROUTE_REGISTRATION

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // ── Pantalla 1: Registro ──
        composable(ROUTE_REGISTRATION) {
            RegistrationScreen(
                viewModel = viewModel,
                context = context,
                onNavigateToQr = {
                    navController.navigate(ROUTE_QR_SYNC) {
                        popUpTo(ROUTE_REGISTRATION) { inclusive = true }
                    }
                }
            )
        }

        // ── Pantalla 2: QR de sincronización ──
        composable(ROUTE_QR_SYNC) {
            QrSyncScreen(
                viewModel = viewModel,
                onActivated = {
                    navController.navigate(ROUTE_PERMISSIONS) {
                        popUpTo(ROUTE_QR_SYNC) { inclusive = true }
                    }
                },
                onReset = {
                    navController.navigate(ROUTE_REGISTRATION) {
                        popUpTo(ROUTE_QR_SYNC) { inclusive = true }
                    }
                }
            )
        }

        // ── Gate: permisos BLE ──
        composable(ROUTE_PERMISSIONS) {
            PermissionsScreen(
                onPermissionsGranted = {
                    navController.navigate(ROUTE_MAIN) {
                        popUpTo(ROUTE_PERMISSIONS) { inclusive = true }
                    }
                }
            )
        }

        // ── Pantalla 4: Dashboard Principal ──
        composable(ROUTE_MAIN) {
            MainScreen(
                viewModel = viewModel,
                onLogout = {
                    navController.navigate(ROUTE_REGISTRATION) {
                        popUpTo(ROUTE_MAIN) { inclusive = true }
                    }
                }
            )
        }
    }
}
