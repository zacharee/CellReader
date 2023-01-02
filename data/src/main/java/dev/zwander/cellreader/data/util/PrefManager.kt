package dev.zwander.cellreader.data.util

import android.annotation.SuppressLint
import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.preferences: PrefManager
    get() = PrefManager.getInstance(this)

class PrefManager private constructor(private val context: Context) {
    companion object {
        @SuppressLint("StaticFieldLeak")
        private var instance: PrefManager? = null

        fun getInstance(context: Context): PrefManager {
            return instance ?: PrefManager(context.applicationContext ?: context).apply {
                instance = this
            }
        }

        val SEND_TO_WEAR = booleanPreferencesKey("send_to_wear")
        val HAS_DECLINED_BACKGROUND_LOCATION = booleanPreferencesKey("declined_background_location")
    }

    val Context.store by preferencesDataStore(
        "settings"
    )

    val sendToWear: Flow<Boolean>
        get() = context.store.data.map { it[SEND_TO_WEAR] ?: false }

    val declinedBackgroundLocation: Flow<Boolean>
        get() = context.store.data.map { it[HAS_DECLINED_BACKGROUND_LOCATION] ?: false }

    suspend fun updateSendToWear(sendToWear: Boolean) {
        context.store.edit {
            it[SEND_TO_WEAR] = sendToWear
        }
    }

    suspend fun updateDeclinedBackgroundLocation(declined: Boolean) {
        context.store.edit {
            it[HAS_DECLINED_BACKGROUND_LOCATION] = declined
        }
    }
}