package dev.zwander.cellreader.data

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import com.google.android.gms.wearable.*
import com.google.gson.reflect.TypeToken
import dev.zwander.cellreader.data.wrappers.*
import dev.zwander.cellreader.data.wrappers.ServiceStateWrapper
import io.gsonfire.GsonFireBuilder
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.tasks.await

class BetweenUtils private constructor(private val context: Context) {
    companion object {
        const val CELL_INFOS_PATH = "/cell_infos"
        const val CELL_SIGNAL_STRENGTHS_PATH = "/cell_signal_strengths"

        const val CELL_INFOS_KEY = "cell_infos"
        const val CELL_SIGNAL_STRENGTHS_KEY = "cell_signal_strengths"

        const val SERVICE_STATE_PATH = "/service_state"
        const val SERVICE_STATE_KEY = "service_state"

        const val SUB_INFO_PATH = "/sub_info"
        const val SUB_INFO_KEY = "sub_info"

        const val SUB_ID_PATH = "/sub_id"
        const val SUB_ID_KEY = "sub_id"

        const val CLEAR_PATH = "/clear"
        const val CLEAR_KEY = "clear"

        const val PRIMARY_CELL_PATH = "/primary_cell"
        const val PRIMARY_CELL_KEY = "primary_cell"

        @SuppressLint("StaticFieldLeak")
        private var instance: BetweenUtils? = null

        fun getInstance(context: Context): BetweenUtils {
            return instance ?: BetweenUtils(context.applicationContext ?: context).apply {
                instance = this
            }
        }
    }

    private val gson = GsonFireBuilder()
        .wrap(CellInfoGsmWrapper::class.java, CellInfoGsmWrapper::class.java.canonicalName)
        .wrap(CellIdentityGsmWrapper::class.java, CellIdentityGsmWrapper::class.java.canonicalName)
        .wrap(CellSignalStrengthGsmWrapper::class.java, CellSignalStrengthGsmWrapper::class.java.canonicalName)
        .wrap(CellInfoCdmaWrapper::class.java, CellInfoCdmaWrapper::class.java.canonicalName)
        .wrap(CellIdentityCdmaWrapper::class.java, CellIdentityCdmaWrapper::class.java.canonicalName)
        .wrap(CellSignalStrengthCdmaWrapper::class.java, CellSignalStrengthCdmaWrapper::class.java.canonicalName)
        .wrap(CellInfoLteWrapper::class.java, CellInfoLteWrapper::class.java.canonicalName)
        .wrap(CellIdentityLteWrapper::class.java, CellIdentityLteWrapper::class.java.canonicalName)
        .wrap(CellSignalStrengthLteWrapper::class.java, CellSignalStrengthLteWrapper::class.java.canonicalName)
        .wrap(CellIdentityTdscdmaWrapper::class.java, CellIdentityTdscdmaWrapper::class.java.canonicalName)
        .wrap(CellInfoWcdmaWrapper::class.java, CellInfoWcdmaWrapper::class.java.canonicalName)
        .wrap(CellIdentityWcdmaWrapper::class.java, CellIdentityWcdmaWrapper::class.java.canonicalName)
        .wrap(CellSignalStrengthWcdmaWrapper::class.java, CellSignalStrengthWcdmaWrapper::class.java.canonicalName)
        .wrap(CellInfoTdscdmaWrapper::class.java, CellInfoTdscdmaWrapper::class.java.canonicalName)
        .wrap(CellSignalStrengthTdscdmaWrapper::class.java, CellSignalStrengthTdscdmaWrapper::class.java.canonicalName)
        .wrap(CellInfoNrWrapper::class.java, CellInfoNrWrapper::class.java.canonicalName)
        .wrap(CellIdentityNrWrapper::class.java, CellIdentityNrWrapper::class.java.canonicalName)
        .wrap(CellSignalStrengthNrWrapper::class.java, CellSignalStrengthNrWrapper::class.java.canonicalName)
        .registerTypeSelector(CellInfoWrapper::class.java) { element ->
            Log.e("CellReader", "TYPE SELECTOR ${element.asJsonObject}")

            val name = element.asJsonObject.keySet().first()

            try {
                @Suppress("UNCHECKED_CAST")
                CellInfoWrapper::class.java.classLoader?.loadClass(name) as Class<out CellInfoWrapper>?
            } catch (e: Exception) {
                Log.e("CellReader", "error creating class", e)
                null
            }
        }
        .registerTypeSelector(CellSignalStrengthWrapper::class.java) { element ->
            Log.e("CellReader", "TYPE SELECTOR ${element.asJsonObject}")

            val name = element.asJsonObject.keySet().first()

            try {
                @Suppress("UNCHECKED_CAST")
                CellSignalStrengthWrapper::class.java.classLoader?.loadClass(name) as Class<out CellSignalStrengthWrapper>?
            } catch (e: Exception) {
                Log.e("CellReader", "error creating class", e)
                null
            }
        }
        .registerTypeSelector(CellIdentityWrapper::class.java) { element ->
            Log.e("CellReader", "TYPE SELECTOR ${element.asJsonObject}")

            val name = element.asJsonObject.keySet().first()

            try {
                @Suppress("UNCHECKED_CAST")
                CellIdentityWrapper::class.java.classLoader?.loadClass(name) as Class<out CellIdentityWrapper>?
            } catch (e: Exception) {
                Log.e("CellReader", "error creating class", e)
                null
            }
        }
        .createGsonBuilder()
        .create()

    private val dataClient = Wearable.getDataClient(context)

    private val cellMutex = Mutex()
    private val signalMutex = Mutex()
    private val stateMutex = Mutex()
    private val subInfoMutex = Mutex()
    private val subIdMutex = Mutex()
    private val clearMutex = Mutex()
    private val primaryMutex = Mutex()
    
    suspend fun sendCellInfos(subId: Int, wrapped: List<CellInfoWrapper>): DataItem {
        return cellMutex.sendInfo(
            CELL_INFOS_PATH,
            listOf(
                CELL_INFOS_KEY to (subId to wrapped)
            )
        )
    }

    suspend fun retrieveCellInfos(dataItem: DataItem): Pair<Int, ArrayList<CellInfoWrapper>> {
        return retrieveInfo(
            dataItem,
            CELL_INFOS_KEY,
            Pair(0, arrayListOf())
        )
    }

    suspend fun sendSignalStrengths(subId: Int, wrapped: List<CellSignalStrengthWrapper>): DataItem {
        return signalMutex.sendInfo(
            CELL_SIGNAL_STRENGTHS_PATH,
            listOf(
                CELL_SIGNAL_STRENGTHS_KEY to (subId to wrapped)
            )
        )
    }

    suspend fun retrieveSignalStrengths(dataItem: DataItem): Pair<Int, ArrayList<CellSignalStrengthWrapper>> {
        return retrieveInfo(
            dataItem,
            CELL_SIGNAL_STRENGTHS_KEY,
            Pair(0, arrayListOf())
        )
    }

    suspend fun sendServiceState(subId: Int, wrapped: ServiceStateWrapper?): DataItem {
        return stateMutex.sendInfo(
            SERVICE_STATE_PATH,
            listOf(
                SERVICE_STATE_KEY to (subId to wrapped)
            )
        )
    }

    suspend fun retrieveServiceState(dataItem: DataItem): Pair<Int, ServiceStateWrapper?>? {
        return retrieveInfo(
            dataItem,
            SERVICE_STATE_KEY,
            null
        )
    }

    suspend fun sendSubscriptionInfo(subId: Int, wrapped: SubscriptionInfoWrapper): DataItem {
        return subInfoMutex.sendInfo(
            SUB_INFO_PATH,
            listOf(
                SUB_INFO_KEY to (subId to wrapped)
            )
        )
    }

    suspend fun retrieveSubscriptionInfo(dataItem: DataItem): Pair<Int, SubscriptionInfoWrapper?> {
        return retrieveInfo(
            dataItem,
            SUB_INFO_KEY,
            Pair(0, null)
        )
    }

    suspend fun sendNewSubId(subId: Int): DataItem {
        return subIdMutex.sendInfo(
            SUB_ID_PATH,
            listOf(
                SUB_ID_KEY to subId
            )
        )
    }

    suspend fun retrieveNewSubId(dataItem: DataItem): Int {
        return retrieveInfo(
            dataItem,
            SUB_ID_KEY,
            0
        )
    }

    suspend fun sendClear(): DataItem {
        return clearMutex.sendInfo(
            CLEAR_PATH,
            listOf(
                CLEAR_KEY to System.currentTimeMillis()
            )
        )
    }

    suspend fun sendPrimaryCell(subId: Int): DataItem {
        return primaryMutex.sendInfo(
            PRIMARY_CELL_PATH,
            listOf(
                PRIMARY_CELL_KEY to subId
            )
        )
    }

    suspend fun retrievePrimaryCell(dataItem: DataItem): Int {
        return retrieveInfo(
            dataItem,
            PRIMARY_CELL_KEY,
            0
        )
    }

    private suspend fun Mutex.sendInfo(path: String, pairs: List<Pair<String, Any>>): DataItem {
        withLock {
            val mapped = pairs.map {
                it.first to Asset.createFromBytes(gson.toJson(it.second).toByteArray())
            }

            val request = PutDataMapRequest.create(path).apply {
                mapped.forEach { (key, item) ->
                    dataMap.putAsset(key, item)
                }
            }.asPutDataRequest()
                .setUrgent()

            return dataClient.putDataItem(request).await()
        }
    }

    private suspend inline fun <reified T : Any?> retrieveInfo(dataItem: DataItem, key: String, empty: T): T {
        val asset = try {
            DataMapItem.fromDataItem(dataItem)
                .dataMap
                .getAsset(key)
        } catch (e: IllegalArgumentException) {
            Log.e("CellReader", "error", e)
            null
        } catch (e: IllegalStateException) {
            Log.e("CellReader", "error", e)
            null
        } ?: return empty

        val response = dataClient.getFdForAsset(asset).await()
        val json = response.inputStream.bufferedReader().use { input -> input.readText() }

        return try {
            gson.fromJson(
                json,
                object : TypeToken<T>() {}.type
            ) ?: empty
        } catch (e: Exception) {
            Log.e("CellReader", "error", e)
            empty
        }
    }
}