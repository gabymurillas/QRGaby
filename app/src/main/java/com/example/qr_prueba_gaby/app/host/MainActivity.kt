package com.example.qr_prueba_gaby.app.host

import android.os.Bundle
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.FragmentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.qr_prueba_gaby.presentation.ui.MainScreen.MainScreen
import com.example.qr_prueba_gaby.presentation.ui.MainScreen.MainViewModel
import com.example.qr_prueba_gaby.presentation.ui.QrSyncScreen.QrSyncScreen
import com.example.qr_prueba_gaby.presentation.ui.QrSyncScreen.SyncViewModel
import com.example.qr_prueba_gaby.presentation.ui.RegistrationScreen.RegistrationScreen
import com.example.qr_prueba_gaby.presentation.ui.RegistrationScreen.RegistrationViewModel
import com.example.qr_prueba_gaby.presentation.ui.SplashScreen.SplashScreen
import com.example.qr_prueba_gaby.data.model.ThemeMode
import com.example.qr_prueba_gaby.presentation.ui.theme.QRPRUEBAGABYTheme
import com.example.qr_prueba_gaby.presentation.ui.theme.ThemeViewModel

import dagger.hilt.android.AndroidEntryPoint

// Rutas de navegación
private const val ROUTE_SPLASH        = "splash"
private const val ROUTE_REGISTRATION  = "registration"
private const val ROUTE_QR_SYNC       = "qr_sync"
private const val ROUTE_MAIN          = "main"

@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        // Barras transparentes; los iconos se adaptan al tema desde QRPRUEBAGABYTheme.
        enableEdgeToEdge()

        setContent {
            // El modo de tema persistido decide entre claro y oscuro.
            val themeViewModel: ThemeViewModel = hiltViewModel()
            val themeMode by themeViewModel.themeMode.collectAsStateWithLifecycle()
            val darkTheme = when (themeMode) {
                ThemeMode.LIGHT  -> false
                ThemeMode.DARK   -> true
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }

            QRPRUEBAGABYTheme(darkTheme = darkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
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

    NavHost(
        navController = navController,
        startDestination = ROUTE_SPLASH
    ) {
        // ── Splash: animación inicial + resolución de destino ──
        composable(ROUTE_SPLASH) {
            val syncViewModel: SyncViewModel = hiltViewModel()
            val isActivated by syncViewModel.isActivatedFlow.collectAsStateWithLifecycle()
            SplashScreen(isActivated = isActivated) { activated ->
                val destination = if (activated) ROUTE_MAIN else ROUTE_REGISTRATION
                navController.navigate(destination) {
                    popUpTo(ROUTE_SPLASH) { inclusive = true }
                }
            }
        }

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
        // El permiso de cámara se solicita dentro de QrSyncScreen, justo antes
        // de abrir el escáner; por eso ya no existe una pantalla de permisos.
        composable(ROUTE_QR_SYNC) {
            val syncVM: SyncViewModel = hiltViewModel()
            QrSyncScreen(
                viewModel = syncVM,
                onActivated = {
                    navController.navigate(ROUTE_MAIN) {
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

        // ── Pantalla 3: Dashboard Principal ──
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
