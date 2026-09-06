package com.mwickes.gratuitinator.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.themeDataStore by preferencesDataStore(name = "theme_prefs")

/**
 * Persists the paper/steel toggle across app restarts (Phase 8). A `null` [darkSteelFlow] value
 * means "never set" — the caller should fall back to [android.content.res.Configuration]'s system
 * dark-theme setting in that case rather than assuming a default.
 */
class ThemePreferences(private val context: Context) {

    private val darkSteelKey = booleanPreferencesKey("dark_steel")

    val darkSteelFlow: Flow<Boolean?> =
        context.themeDataStore.data.map { prefs -> prefs[darkSteelKey] }

    suspend fun setDarkSteel(value: Boolean) {
        context.themeDataStore.edit { prefs -> prefs[darkSteelKey] = value }
    }
}
