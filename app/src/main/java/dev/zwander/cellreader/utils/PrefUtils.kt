package dev.zwander.cellreader.utils

import android.content.Context
import android.telephony.*
import android.util.Log
import androidx.datastore.preferences.core.Preferences
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dev.zwander.cellreader.SignalWidget
import io.gsonfire.GsonFireBuilder

object PrefUtils {
    private val gson: Gson = GsonFireBuilder()
        .wrap(CellInfoGsm::class.java, CellInfoGsm::class.java.canonicalName)
        .wrap(CellIdentityGsm::class.java, CellIdentityGsm::class.java.canonicalName)
        .wrap(CellSignalStrengthGsm::class.java, CellSignalStrengthGsm::class.java.canonicalName)
        .wrap(CellInfoCdma::class.java, CellInfoCdma::class.java.canonicalName)
        .wrap(CellIdentityCdma::class.java, CellIdentityCdma::class.java.canonicalName)
        .wrap(CellSignalStrengthCdma::class.java, CellSignalStrengthCdma::class.java.canonicalName)
        .wrap(CellInfoLte::class.java, CellInfoLte::class.java.canonicalName)
        .wrap(CellIdentityLte::class.java, CellIdentityLte::class.java.canonicalName)
        .wrap(CellSignalStrengthLte::class.java, CellSignalStrengthLte::class.java.canonicalName)
        .wrap(CellInfoTdscdma::class.java, CellInfoTdscdma::class.java.canonicalName)
        .wrap(CellIdentityTdscdma::class.java, CellIdentityTdscdma::class.java.canonicalName)
        .wrap(CellSignalStrengthTdscdma::class.java, CellSignalStrengthTdscdma::class.java.canonicalName)
        .wrap(CellInfoWcdma::class.java, CellInfoWcdma::class.java.canonicalName)
        .wrap(CellIdentityWcdma::class.java, CellIdentityWcdma::class.java.canonicalName)
        .wrap(CellSignalStrengthWcdma::class.java, CellSignalStrengthWcdma::class.java.canonicalName)
        .wrap(CellInfoNr::class.java, CellInfoNr::class.java.canonicalName)
        .wrap(CellIdentityNr::class.java, CellIdentityNr::class.java.canonicalName)
        .wrap(CellSignalStrengthNr::class.java, CellSignalStrengthNr::class.java.canonicalName)
        .registerTypeSelector(CellInfo::class.java) { element ->
            val name = element.asJsonObject.keySet().first()

            try {
                @Suppress("UNCHECKED_CAST")
                CellInfo::class.java.classLoader?.loadClass(name) as Class<out CellInfo>?
            } catch (e: Exception) {
                null
            }
        }
        .createGsonBuilder()
        .create()

    fun getCellInfo(prefs: Preferences): Pair<HashMap<Int, List<CellInfo>>, Int> {
        return (gson.fromJson(
            prefs[SignalWidget.cellInfoKey] ?: "",
            object : TypeToken<HashMap<Int, List<CellInfo>>>() {}.type
        ) ?: HashMap<Int, List<CellInfo>>()) to (prefs[SignalWidget.primaryCellKey] ?: 0)
    }

    suspend fun setCellInfos(context: Context, newInfos: Map<Int, List<CellInfo>>, primary: Int) {
        val serialized = gson.toJson(newInfos)

        GlanceAppWidgetManager(context).getGlanceIds(SignalWidget::class.java)
            .forEach { id ->
                updateAppWidgetState(
                    context,
                    PreferencesGlanceStateDefinition,
                    id
                ) { prefs ->
                    prefs.toMutablePreferences().apply {
                        set(SignalWidget.cellInfoKey, serialized)
                        set(SignalWidget.primaryCellKey, primary)
                    }
                }
            }
    }
}