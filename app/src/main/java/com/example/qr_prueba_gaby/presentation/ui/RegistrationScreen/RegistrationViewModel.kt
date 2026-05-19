package com.example.qr_prueba_gaby.presentation.ui.RegistrationScreen

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qr_prueba_gaby.data.model.UserData
import com.example.qr_prueba_gaby.data.pref.UserDataStore
import com.example.qr_prueba_gaby.utils.CryptoManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegistrationViewModel @Inject constructor(
    private val dataStore: UserDataStore,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _nombre = MutableStateFlow("")
    val nombre: StateFlow<String> = _nombre.asStateFlow()

    private val _cedula = MutableStateFlow("")
    val cedula: StateFlow<String> = _cedula.asStateFlow()

    private val _plates = MutableStateFlow(listOf(""))
    val plates: StateFlow<List<String>> = _plates.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _registrationError = MutableStateFlow<String?>(null)
    val registrationError: StateFlow<String?> = _registrationError.asStateFlow()

    fun onNombreChange(value: String) {
        // Solo letras sin acentos y espacios: descarta acentos, ñ, dígitos y
        // caracteres especiales que podrían romper el JSON del QR del conductor.
        _nombre.value = value.filter { it in 'a'..'z' || it in 'A'..'Z' || it == ' ' }
        _registrationError.value = null
    }

    fun onCedulaChange(newCedula: String) {
        val filtered = newCedula.filter { it.isDigit() }
        if (filtered.length <= 10) {
            _cedula.value = filtered
            _registrationError.value = null
        }
    }

    fun onPlateChange(index: Int, value: String) {
        // Solo letras y dígitos (sin acentos, guiones ni caracteres especiales).
        val sanitized = value.uppercase()
            .filter { it in 'A'..'Z' || it in '0'..'9' }
            .take(8)
        val updated = _plates.value.toMutableList()
        if (index < updated.size) {
            updated[index] = sanitized
        }
        _plates.value = updated
        _registrationError.value = null
    }

    fun addPlate() {
        if (_plates.value.size < 5) {
            _plates.value = _plates.value + ""
        }
    }

    fun removePlate(index: Int) {
        if (_plates.value.size > 1) {
            _plates.value = _plates.value.toMutableList().also { it.removeAt(index) }
        }
    }

    @SuppressLint("HardwareIds")
    fun register(onSuccess: () -> Unit) {
        val nombreTrimmed = _nombre.value.trim()
        if (nombreTrimmed.split("\\s+".toRegex()).size < 2) {
            _registrationError.value = "Ingresa tu Nombre y Apellido completo."
            return
        }
        if (_cedula.value.length < 6) {
            _registrationError.value = "Cédula incompleta."
            return
        }
        if (_plates.value.any { it.isBlank() }) {
            _registrationError.value = "Completa todas las placas o elimina las vacías."
            return
        }

        viewModelScope.launch {
            _isGenerating.value = true
            try {
                val androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
                val aidEncrypted = CryptoManager.encrypt(androidId)

                val userData = UserData(
                    u = nombreTrimmed,
                    c = _cedula.value,
                    p = _plates.value.filter { it.isNotBlank() },
                    aid = aidEncrypted
                )

                dataStore.saveUser(userData)
                onSuccess()
            } catch (e: Exception) {
                _registrationError.value = "Error al registrar: ${e.localizedMessage}"
            } finally {
                _isGenerating.value = false
            }
        }
    }
}