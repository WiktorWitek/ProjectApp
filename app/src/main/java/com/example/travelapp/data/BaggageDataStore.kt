package com.example.travelapp.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

val Context.dataStore by preferencesDataStore("baggage_prefs")

object BagPreferences {
    val baggageKey = booleanPreferencesKey("baggage_checked")
}

class BagDataStore(private val context: Context) {
    suspend fun saveBaggageState(context: Context, baggageList: List<BaggageItem>) {
        context.dataStore.edit { preferences ->
            baggageList.forEach { item ->
                val key = booleanPreferencesKey(item.name)
                preferences[key] = item.isChecked.value
            }
        }
    }

    suspend fun loadBaggageState(context: Context, baggageList: List<BaggageItem>) {
        val preferences = context.dataStore.data.first()
        baggageList.forEach { item ->
            val key = booleanPreferencesKey(item.name)
            item.isChecked.value = preferences[key] ?: false
        }
    }
}