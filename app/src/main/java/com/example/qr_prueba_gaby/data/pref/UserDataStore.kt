package com.example.qr_prueba_gaby.data.pref

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.qr_prueba_gaby.data.model.UserData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "qr_gaby_prefs")

/**
 * Repositorio para persistir el estado de registro y datos del usuario
 * mediante Jetpack DataStore (Preferences).
 */
class UserDataStore(private val context: Context) {

    companion object {
        private val KEY_IS_REGISTERED   = booleanPreferencesKey("is_registered")
        private val KEY_IS_ACTIVATED    = booleanPreferencesKey("is_activated")
        private val KEY_USER_JSON       = stringPreferencesKey("user_json")
        private val KEY_USER_PIN        = stringPreferencesKey("user_pin")
        // Provisionamiento por QR del Administrador
        private val KEY_URL_ENDPOINT    = stringPreferencesKey("url_endpoint")
    }

    /** Flow que emite true si el usuario ya está registrado y activo. */
    val isRegisteredFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_IS_REGISTERED] ?: false
    }

    /** Flow que emite true si el dispositivo ha sido activado vía BLE. */
    val isActivatedFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_IS_ACTIVATED] ?: false
    }

    /** Flow que emite el PIN del usuario. */
    val userPinFlow: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[KEY_USER_PIN]
    }

    /** Flow que emite los datos del usuario, o null si no está registrado. */
    val userDataFlow: Flow<UserData?> = context.dataStore.data.map { prefs ->
        prefs[KEY_USER_JSON]?.let { UserData.fromJson(it) }
    }

    /** Flow con el endpoint recibido en el QR de aprovisionamiento, o null si aún no se recibió. */
    val urlEndpointFlow: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[KEY_URL_ENDPOINT]
    }

    /** Persiste los datos del usuario en DataStore. */
    suspend fun saveUser(userData: UserData) {
        context.dataStore.edit { prefs ->
            prefs[KEY_USER_JSON] = userData.toJson()
        }
    }

    /** Guarda el PIN de seguridad. */
    suspend fun savePin(pin: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_USER_PIN] = pin
        }
    }

    /** Elimina el PIN de seguridad (lo desactiva). */
    suspend fun clearPin() {
        context.dataStore.edit { prefs ->
            prefs.remove(KEY_USER_PIN)
        }
    }

    /** Marca al usuario como activo/inactivo. */
    suspend fun setRegistered(registered: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_IS_REGISTERED] = registered
        }
    }

    /** Marca el dispositivo como activado/desactivado vía BLE. */
    suspend fun setActivated(activated: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_IS_ACTIVATED] = activated
        }
    }

    /**
     * Persiste el endpoint del backend Odoo recibido en el QR de aprovisionamiento.
     */
    suspend fun saveProvisioningData(endpoint: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_URL_ENDPOINT] = endpoint
        }
    }

    /** Limpia todos los datos (para logout o reinstalación). */
    suspend fun clearAll() {
        context.dataStore.edit { it.clear() }
    }
}
