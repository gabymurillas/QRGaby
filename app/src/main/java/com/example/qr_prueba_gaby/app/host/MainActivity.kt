package com.example.qr_prueba_gaby.app.host

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.qr_prueba_gaby.presentation.ui.screens.MainScreen
import com.example.qr_prueba_gaby.presentation.ui.screens.PermissionsScreen
import com.example.qr_prueba_gaby.presentation.ui.screens.QrSyncScreen
import com.example.qr_prueba_gaby.presentation.ui.screens.RegistrationScreen
import com.example.qr_prueba_gaby.presentation.ui.theme.QRPRUEBAGABYTheme
import com.example.qr_prueba_gaby.presentation.ui.viewmodels.*
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
        
        // Configurar Barras Transparentes con Iconos Oscuros (para fondo claro)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            ),
            navigationBarStyle = SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            )
        )
        
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
    val navController = rememberNavController()

    // Usamos el SyncViewModel solo para chequear el estado inicial de activación
    val syncViewModel: SyncViewModel = hiltViewModel()
    val isActivated by syncViewModel.isActivatedFlow.collectAsStateWithLifecycle()

    // Pantalla de carga mientras se determina el destino inicial
    if (isActivated == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Color.Cyan)
        }
        return
    }

    val startDestination = if (isActivated == true) ROUTE_MAIN else ROUTE_REGISTRATION

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // ── Pantalla 1: Registro ──
        composable(ROUTE_REGISTRATION) {
            val regViewModel: RegistrationViewModel = hiltViewModel()
            RegistrationScreen(
                viewModel = regViewModel,
                onNavigateToQr = {
                    navController.navigate(ROUTE_QR_SYNC) {
                        popUpTo(ROUTE_REGISTRATION) { inclusive = true }
                    }
                }
            )
        }

        // ── Pantalla 2: QR de sincronización ──
        composable(ROUTE_QR_SYNC) {
            val syncVM: SyncViewModel = hiltViewModel()
            QrSyncScreen(
                viewModel = syncVM,
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
            val mainVM: MainViewModel = hiltViewModel()
            MainScreen(
                viewModel = mainVM,
                onLogout = {
                    mainVM.logout {
                        navController.navigate(ROUTE_REGISTRATION) {
                            popUpTo(ROUTE_MAIN) { inclusive = true }
                        }
                    }
                }
            )
        }
    }
}
