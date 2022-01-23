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
    private val subInfoMutext = Mutex()
    
    suspend fun sendCellInfos(subId: Int, wrapped: List<CellInfoWrapper>): DataItem {
        cellMutex.withLock {
            val json = gson.toJson(subId to wrapped)
            val asset = Asset.createFromBytes(json.toByteArray())
            val request = PutDataMapRequest.create(CELL_INFOS_PATH).apply {
                dataMap.putAsset(CELL_INFOS_KEY, asset)
            }.asPutDataRequest()
                .setUrgent()

            return dataClient.putDataItem(request).await()
        }
    }

    suspend fun retrieveCellInfos(dataItem: DataItem): Pair<Int, ArrayList<CellInfoWrapper>> {
        val asset = try {
            DataMapItem.fromDataItem(dataItem)
                .dataMap
                .getAsset(CELL_INFOS_KEY)
        } catch (e: IllegalArgumentException) {
            Log.e("CellReader", "error", e)
            null
        } catch (e: IllegalStateException) {
            Log.e("CellReader", "error", e)
            null
        } ?: return Pair(0, arrayListOf())

        val response = dataClient.getFdForAsset(asset).await()
        val json = response.inputStream.bufferedReader().use { input -> input.readText() }
        return try {
            gson.fromJson<Pair<Int, ArrayList<CellInfoWrapper>>>(
                json,
                object : TypeToken<Pair<Int, ArrayList<CellInfoWrapper>>>() {}.type
            ) ?: Pair(0, arrayListOf())
        } catch (e: Exception) {
            Log.e("CellReader", "error", e)
            Pair(0, arrayListOf())
        }
    }

    suspend fun sendSignalStrengths(subId: Int, wrapped: List<CellSignalStrengthWrapper>): DataItem {
        signalMutex.withLock {
            val json = gson.toJson(subId to wrapped)
            val asset = Asset.createFromBytes(json.toByteArray())
            val request = PutDataMapRequest.create(CELL_SIGNAL_STRENGTHS_PATH).apply {
                dataMap.putAsset(CELL_SIGNAL_STRENGTHS_KEY, asset)
            }.asPutDataRequest()

            return dataClient.putDataItem(request).await()
        }
    }

    suspend fun retrieveSignalStrengths(dataItem: DataItem): Pair<Int, ArrayList<CellSignalStrengthWrapper>> {
        val asset = try {
            DataMapItem.fromDataItem(dataItem)
                .dataMap
                .getAsset(CELL_SIGNAL_STRENGTHS_KEY)
        } catch (e: IllegalArgumentException) {
            Log.e("CellReader", "error", e)
            null
        } catch (e: IllegalStateException) {
            Log.e("CellReader", "error", e)
            null
        } ?: return Pair(0, arrayListOf())

        val response = dataClient.getFdForAsset(asset).await()
        val json = response.inputStream.bufferedReader().use { input -> input.readText() }
        return try {
            gson.fromJson<Pair<Int, ArrayList<CellSignalStrengthWrapper>>>(
                json,
                object : TypeToken<Pair<Int, ArrayList<CellSignalStrengthWrapper>>>() {}.type
            ) ?: Pair(0, arrayListOf())
        } catch (e: Exception) {
            Log.e("CellReader", "error", e)
            Pair(0, arrayListOf())
        }
    }

    suspend fun sendServiceState(subId: Int, wrapped: ServiceStateWrapper?): DataItem {
        stateMutex.withLock {
            val json = gson.toJson(subId to wrapped)
            val asset = Asset.createFromBytes(json.toByteArray())
            val request = PutDataMapRequest.create(SERVICE_STATE_PATH).apply {
                dataMap.putAsset(SERVICE_STATE_KEY, asset)
            }.asPutDataRequest()

            return dataClient.putDataItem(request).await()
        }
    }

    suspend fun retrieveServiceState(dataItem: DataItem): Pair<Int, ServiceStateWrapper?>? {
        val asset = try {
            DataMapItem.fromDataItem(dataItem)
                .dataMap
                .getAsset(SERVICE_STATE_KEY)
        } catch (e: IllegalArgumentException) {
            Log.e("CellReader", "error", e)
            null
        } catch (e: IllegalStateException) {
            Log.e("CellReader", "error", e)
            null
        } ?: return null

        val response = dataClient.getFdForAsset(asset).await()
        val json = response.inputStream.bufferedReader().use { input -> input.readText() }
        return try {
            gson.fromJson<Pair<Int, ServiceStateWrapper?>>(
                json,
                object : TypeToken<Pair<Int, ServiceStateWrapper?>>() {}.type
            )
        } catch (e: Exception) {
            Log.e("CellReader", "error", e)
            null
        }
    }

    suspend fun sendSubscriptionInfo(subId: Int, wrapped: SubscriptionInfoWrapper): DataItem {
        subInfoMutext.withLock {
            val json = gson.toJson(subId to wrapped)
            val asset = Asset.createFromBytes(json.toByteArray())
            val request = PutDataMapRequest.create(SUB_INFO_PATH).apply {
                dataMap.putAsset(SUB_INFO_KEY, asset)
            }.asPutDataRequest()

            return dataClient.putDataItem(request).await()
        }
    }
}