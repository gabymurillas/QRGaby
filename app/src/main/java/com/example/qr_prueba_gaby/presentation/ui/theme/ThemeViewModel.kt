package com.example.qr_prueba_gaby.presentation.ui.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qr_prueba_gaby.data.model.ThemeMode
import com.example.qr_prueba_gaby.data.pref.UserDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * ViewModel mínimo a nivel de aplicación.
 *
 * Expone el modo de tema persistido para que [com.example.qr_prueba_gaby.app.host.MainActivity]
 * elija entre el esquema claro y el oscuro antes de componer la UI.
 */
@HiltViewModel
class ThemeViewModel @Inject constructor(
    dataStore: UserDataStore
) : ViewModel() {

    val themeMode: StateFlow<ThemeMode> = dataStore.themeModeFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ThemeMode.SYSTEM)
}
