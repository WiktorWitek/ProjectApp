package com.example.travelapp.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map



val Context.userPreferencesDataStore by preferencesDataStore("user_preferences")

object PreferencesKeys {
    val IS_DARK_MODE = booleanPreferencesKey("is_dark_mode")
}

class ThemePreferences(private val context: Context) {

    val isDarkMode: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.IS_DARK_MODE] ?: false // Domyślnie false
        }

    suspend fun saveDarkModeSetting(isDarkMode: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_DARK_MODE] = isDarkMode
        }
    }
}