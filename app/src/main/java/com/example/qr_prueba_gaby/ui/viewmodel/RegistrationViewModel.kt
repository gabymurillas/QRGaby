package com.example.qr_prueba_gaby.ui.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qr_prueba_gaby.data.CryptoManager
import com.example.qr_prueba_gaby.data.UserDataStore
import com.example.qr_prueba_gaby.data.model.UserData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RegistrationState(
    val name: String = "",
    val cedula: String = "",
    val phone: String = "",
    val plates: List<String> = listOf(""),
    val isSaving: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class RegistrationViewModel @Inject constructor(
    private val userDataStore: UserDataStore
) : ViewModel() {

    private val _state = MutableStateFlow(RegistrationState())
    val state: StateFlow<RegistrationState> = _state.asStateFlow()

    fun onNameChange(newName: String) {
        _state.update { it.copy(name = newName) }
    }

    fun onCedulaChange(newCedula: String) {
        _state.update { it.copy(cedula = newCedula) }
    }

    fun onPhoneChange(newPhone: String) {
        _state.update { it.copy(phone = newPhone) }
    }

    fun onPlateChange(index: Int, newPlate: String) {
        _state.update {
            val newPlates = it.plates.toMutableList()
            newPlates[index] = newPlate
            it.copy(plates = newPlates)
        }
    }

    fun addPlateField() {
        _state.update { it.copy(plates = it.plates + "") }
    }

    fun removePlateField(index: Int) {
        _state.update {
            val newPlates = it.plates.toMutableList()
            if (newPlates.size > 1) newPlates.removeAt(index)
            it.copy(plates = newPlates)
        }
    }

    @SuppressLint("HardwareIds")
    fun registerUser(context: Context, onSuccess: () -> Unit) {
        val currentState = _state.value
        if (currentState.name.isBlank() || currentState.cedula.isBlank() || currentState.plates.any { it.isBlank() }) {
            _state.update { it.copy(error = "Por favor completa todos los campos.") }
            return
        }

        _state.update { it.copy(isSaving = true, error = null) }

        viewModelScope.launch {
            try {
                // 1. Obtener ANDROID_ID
                val androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID) ?: "unknown"
                
                // 2. Encriptar con la nueva llave compartida
                val encryptedAid = CryptoManager.encrypt(androidId)
                
                // 3. Crear UserData con los nombres de campos cortos (u, c, p, aid)
                val userData = UserData(
                    u = currentState.name,
                    c = currentState.cedula,
                    t = currentState.phone,
                    p = currentState.plates.filter { it.isNotBlank() },
                    aid = encryptedAid
                )

                // 4. Guardar usando el método correcto 'saveUser'
                userDataStore.saveUser(userData)
                onSuccess()
            } catch (e: Exception) {
                _state.update { it.copy(isSaving = false, error = "Error al registrar: ${e.message}") }
            }
        }
    }
}
