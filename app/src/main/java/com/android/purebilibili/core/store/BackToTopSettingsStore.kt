package com.android.purebilibili.core.store

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

const val DEFAULT_BACK_TO_TOP_BUTTON_ENABLED = true

object BackToTopSettingsStore {
    private val enabledKey = booleanPreferencesKey("back_to_top_button_enabled")

    fun isEnabled(context: Context): Flow<Boolean> = context.settingsDataStore.data
        .map { preferences ->
            preferences[enabledKey] ?: DEFAULT_BACK_TO_TOP_BUTTON_ENABLED
        }

    suspend fun setEnabled(context: Context, enabled: Boolean) {
        context.settingsDataStore.edit { preferences ->
            preferences[enabledKey] = enabled
        }
    }
}
