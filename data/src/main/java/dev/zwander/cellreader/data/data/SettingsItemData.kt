package dev.zwander.cellreader.data.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.flow.Flow

data class SettingsItemData<T : Any?>(
    val nameRes: Int,
    val key: Preferences.Key<T>,
    val default: T
)

data class ReorderSettingsItemData(
    val nameRes: Int,
    val initialValue: Context.() -> Flow<List<CellSignalInfo.Keys<*>>>,
    val onSave: suspend Context.(newOrder: List<CellSignalInfo.Keys<*>>) -> Unit
)
