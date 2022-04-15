package dev.zwander.cellreader.data.data

import androidx.datastore.preferences.core.Preferences

data class SettingsItemData<T : Any?>(
    val nameRes: Int,
    val key: Preferences.Key<T>,
    val default: T
)