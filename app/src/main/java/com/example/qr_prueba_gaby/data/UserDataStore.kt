package com.example.qr_prueba_gaby.data

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
        private val KEY_IS_REGISTERED = booleanPreferencesKey("is_registered")
        private val KEY_USER_JSON = stringPreferencesKey("user_json")
    }

    /** Flow que emite true si el usuario ya está registrado y activo. */
    val isRegisteredFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_IS_REGISTERED] ?: false
    }

    /** Flow que emite los datos del usuario, o null si no está registrado. */
    val userDataFlow: Flow<UserData?> = context.dataStore.data.map { prefs ->
        prefs[KEY_USER_JSON]?.let { UserData.fromJson(it) }
    }

    /** Persiste los datos del usuario en DataStore. */
    suspend fun saveUser(userData: UserData) {
        context.dataStore.edit { prefs ->
            prefs[KEY_USER_JSON] = userData.toJson()
        }
    }

    /** Marca al usuario como activo/inactivo. */
    suspend fun setRegistered(registered: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_IS_REGISTERED] = registered
        }
    }

    /** Limpia todos los datos (para logout o reinstalación). */
    suspend fun clearAll() {
        context.dataStore.edit { it.clear() }
    }
}
