package com.example.mocopraktikum.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val USER_NAME = stringPreferencesKey("user_name")
        val IS_DARK_MODE = booleanPreferencesKey("is_dark_mode")
        val LAST_VIEWED_SPOT_ID = stringPreferencesKey("last_viewed_spot_id")
        val LAST_LATITUDE = doublePreferencesKey("last_latitude")
        val LAST_LONGITUDE = doublePreferencesKey("last_longitude")
    }

    val lastLocation: Flow<Pair<Double, Double>?> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val lat = preferences[PreferencesKeys.LAST_LATITUDE]
            val lon = preferences[PreferencesKeys.LAST_LONGITUDE]
            if (lat != null && lon != null) Pair(lat, lon) else null
        }

    val lastViewedSpotId: Flow<String?> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.LAST_VIEWED_SPOT_ID]
        }

    val userName: Flow<String> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.USER_NAME] ?: "Gast"
        }

    val isDarkMode: Flow<Boolean> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.IS_DARK_MODE] ?: false
        }

    suspend fun saveUserName(name: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.USER_NAME] = name
        }
    }

    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_DARK_MODE] = enabled
        }
    }

    suspend fun saveLastViewedSpotId(id: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LAST_VIEWED_SPOT_ID] = id
        }
    }

    suspend fun saveLastLocation(lat: Double, lon: Double) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LAST_LATITUDE] = lat
            preferences[PreferencesKeys.LAST_LONGITUDE] = lon
        }
    }
}
