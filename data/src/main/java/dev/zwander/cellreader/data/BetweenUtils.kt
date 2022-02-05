package dev.zwander.cellreader.data

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.GoogleApiAvailabilityLight
import com.google.android.gms.common.GooglePlayServicesUtil
import com.google.android.gms.common.api.Api
import com.google.android.gms.common.api.AvailabilityException
import com.google.android.gms.common.api.GoogleApi
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.wearable.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dev.zwander.cellreader.data.wrappers.*
import dev.zwander.cellreader.data.wrappers.ServiceStateWrapper
import io.gsonfire.GsonFireBuilder
import io.gsonfire.TypeSelector
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.tasks.await
import java.util.concurrent.ConcurrentHashMap

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

    private val cellIdentitySelector = TypeSelector { element ->
        val name = element.asJsonObject.keySet().first()

        try {
            @Suppress("UNCHECKED_CAST")
            CellIdentityWrapper::class.java.classLoader?.loadClass(name) as Class<out CellIdentityWrapper>?
        } catch (e: Exception) {
            Log.e("CellReader", "error creating class", e)
            null
        }
    }

    private val vopsSupportInfoSelector = TypeSelector { element ->
        val name = element.asJsonObject.keySet().first()

        try {
            @Suppress("UNCHECKED_CAST")
            VopsSupportInfoWrapper::class.java.classLoader?.loadClass(name) as Class<out VopsSupportInfoWrapper>?
        } catch (e: Exception) {
            Log.e("CellReader", "error creating class", e)
            null
        }
    }

    private val cellSignalStrengthSelector = TypeSelector { element ->
        val name = element.asJsonObject.keySet().first()

        try {
            @Suppress("UNCHECKED_CAST")
            CellSignalStrengthWrapper::class.java.classLoader?.loadClass(name) as Class<out CellSignalStrengthWrapper>?
        } catch (e: Exception) {
            Log.e("CellReader", "error creating class", e)
            null
        }
    }

    private val cellInfoSelector = TypeSelector { element ->
        val name = element.asJsonObject.keySet().first()

        try {
            @Suppress("UNCHECKED_CAST")
            CellInfoWrapper::class.java.classLoader?.loadClass(name) as Class<out CellInfoWrapper>?
        } catch (e: Exception) {
            Log.e("CellReader", "error creating class", e)
            null
        }
    }

    private val baseWrapperGson: GsonFireBuilder
        get() = GsonFireBuilder()
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
            .wrap(LteVopsSupportInfoWrapper::class.java, LteVopsSupportInfoWrapper::class.java.canonicalName)
            .wrap(NrVopsSupportInfoWrapper::class.java, NrVopsSupportInfoWrapper::class.java.canonicalName)
            .wrap(ServiceStateWrapper::class.java, ServiceStateWrapper::class.java.canonicalName)

    private val cellInfoGson = baseWrapperGson
        .registerTypeSelector(CellInfoWrapper::class.java, cellInfoSelector)
        .createGsonBuilder()
        .create()

    private val serviceStateGson = baseWrapperGson
        .registerTypeSelector(CellIdentityWrapper::class.java, cellIdentitySelector)
        .registerTypeSelector(VopsSupportInfoWrapper::class.java, vopsSupportInfoSelector)
        .createGsonBuilder()
        .create()

    private val cellSignalStrengthGson = baseWrapperGson
        .registerTypeSelector(CellSignalStrengthWrapper::class.java, cellSignalStrengthSelector)
        .createGsonBuilder()
        .create()

    private val otherGson = baseWrapperGson
        .createGsonBuilder()
        .create()

    private val queueMutex = Mutex()

    private val cellMutex = Mutex()
    private val signalMutex = Mutex()
    private val stateMutex = Mutex()
    private val subInfoMutex = Mutex()
    private val subIdMutex = Mutex()
    private val clearMutex = Mutex()
    private val primaryMutex = Mutex()

    private val sendQueue = ConcurrentHashMap<Mutex, suspend () -> Unit>()
    private var lastSendTime = 0L

    private suspend fun getDataClient(): DataClient? {
        val avail = try {
            GoogleApiAvailability.getInstance().checkApiAvailability(
                GoogleApi(
                    context,
                    Wearable.API,
                    Wearable.WearableOptions.Builder().build(),
                    GoogleApi.Settings.DEFAULT_SETTINGS
                )
            ).await()

            true
        } catch (e: AvailabilityException) {
            false
        }

        return if (avail) {
            Wearable.getDataClient(context)
        } else {
            null
        }
    }

    suspend fun queueCellInfos(infos: Map<Int, List<CellInfoWrapper>>) {
        enqueue(cellMutex) {
            sendCellInfos(infos)
        }
    }

    private suspend fun sendCellInfos(infos: Map<Int, List<CellInfoWrapper>>) {
        cellMutex.sendInfo(
            cellInfoGson,
            CELL_INFOS_PATH,
            listOf(
                CELL_INFOS_KEY to infos
            )
        )
    }

    suspend fun retrieveCellInfos(dataItem: DataItem): HashMap<Int, ArrayList<CellInfoWrapper>> {
        return retrieveInfo(
            cellInfoGson,
            dataItem,
            CELL_INFOS_KEY,
            hashMapOf(),
            object : TypeToken<HashMap<Int, ArrayList<CellInfoWrapper>>>() {}
        )
    }

    suspend fun queueSignalStrengths(strengths: Map<Int, List<CellSignalStrengthWrapper>>) {
        enqueue(signalMutex) {
            sendSignalStrengths(strengths)
        }
    }

    private suspend fun sendSignalStrengths(strengths: Map<Int, List<CellSignalStrengthWrapper>>) {
        signalMutex.sendInfo(
            cellSignalStrengthGson,
            CELL_SIGNAL_STRENGTHS_PATH,
            listOf(
                CELL_SIGNAL_STRENGTHS_KEY to strengths
            )
        )
    }

    suspend fun retrieveSignalStrengths(dataItem: DataItem): HashMap<Int, ArrayList<CellSignalStrengthWrapper>> {
        return retrieveInfo(
            cellSignalStrengthGson,
            dataItem,
            CELL_SIGNAL_STRENGTHS_KEY,
            hashMapOf(),
            object : TypeToken<HashMap<Int, ArrayList<CellSignalStrengthWrapper>>>() {}
        )
    }

    suspend fun queueServiceState(states: Map<Int, ServiceStateWrapper?>) {
        enqueue(stateMutex) {
            sendServiceState(states)
        }
    }

    private suspend fun sendServiceState(states: Map<Int, ServiceStateWrapper?>) {
        stateMutex.sendInfo(
            serviceStateGson,
            SERVICE_STATE_PATH,
            listOf(
                SERVICE_STATE_KEY to states
            )
        )
    }

    suspend fun retrieveServiceState(dataItem: DataItem): HashMap<Int, ServiceStateWrapper?> {
        return retrieveInfo(
            serviceStateGson,
            dataItem,
            SERVICE_STATE_KEY,
            hashMapOf(),
            object : TypeToken<HashMap<Int, ServiceStateWrapper?>>() {}
        )
    }

    suspend fun queueSubscriptionInfo(infos: Map<Int, SubscriptionInfoWrapper?>) {
        enqueue(subInfoMutex) {
            sendSubscriptionInfo(infos)
        }
    }

    private suspend fun sendSubscriptionInfo(infos: Map<Int, SubscriptionInfoWrapper?>) {
        subInfoMutex.sendInfo(
            otherGson,
            SUB_INFO_PATH,
            listOf(
                SUB_INFO_KEY to infos
            )
        )
    }

    suspend fun retrieveSubscriptionInfo(dataItem: DataItem): HashMap<Int, SubscriptionInfoWrapper?> {
        return retrieveInfo(
            otherGson,
            dataItem,
            SUB_INFO_KEY,
            hashMapOf(),
            object : TypeToken<HashMap<Int, SubscriptionInfoWrapper?>>() {}
        )
    }

    suspend fun queueNewSubId(currentIds: List<Int>) {
        enqueue(subIdMutex) {
            sendNewSubId(currentIds)
        }
    }

    private suspend fun sendNewSubId(currentIds: List<Int>) {
        subIdMutex.sendInfo(
            otherGson,
            SUB_ID_PATH,
            listOf(
                SUB_ID_KEY to currentIds
            )
        )
    }

    suspend fun retrieveNewSubId(dataItem: DataItem): ArrayList<Int> {
        return retrieveInfo(
            otherGson,
            dataItem,
            SUB_ID_KEY,
            arrayListOf(),
            object : TypeToken<ArrayList<Int>>() {}
        )
    }

    suspend fun queueClear() {
        enqueue(clearMutex) {
            sendClear()
        }
    }

    private suspend fun sendClear() {
        clearMutex.sendInfo(
            otherGson,
            CLEAR_PATH,
            listOf(
                CLEAR_KEY to System.currentTimeMillis()
            )
        )
    }

    suspend fun queuePrimaryCell(subId: Int) {
        enqueue(primaryMutex) {
            sendPrimaryCell(subId)
        }
    }

    private suspend fun sendPrimaryCell(subId: Int) {
        primaryMutex.sendInfo(
            otherGson,
            PRIMARY_CELL_PATH,
            listOf(
                PRIMARY_CELL_KEY to subId
            )
        )
    }

    suspend fun retrievePrimaryCell(dataItem: DataItem): Int {
        return retrieveInfo(
            otherGson,
            dataItem,
            PRIMARY_CELL_KEY,
            0,
            object : TypeToken<Int>() {}
        )
    }

    private suspend fun enqueue(mutex: Mutex, block: suspend () -> Unit) {
        queueMutex.withLock {
            sendQueue[mutex] = block

            dispatch()
        }
    }

    private suspend fun dispatch() {
        val currentTime = System.currentTimeMillis()

        if (currentTime - lastSendTime > 1000L) {
            lastSendTime = currentTime

            sendQueue.forEach { (_, block) ->
                block()
            }
            sendQueue.clear()
        }
    }

    private suspend fun Mutex.sendInfo(gson: Gson, path: String, pairs: List<Pair<String, Any>>): DataItem? {
        withLock {
            val mapped = pairs.map {
                it.first to gson.toJson(it.second).toByteArray()
            }

            val request = PutDataMapRequest.create(path).apply {
                mapped.forEach { (key, item) ->
                    dataMap.putByteArray(key, item)
                }
            }.asPutDataRequest()
                .setUrgent()

            return getDataClient()?.putDataItem(request)?.await()
        }
    }

    private inline fun <reified T : Any?> retrieveInfo(
        gson: Gson,
        dataItem: DataItem,
        key: String,
        empty: T,
        typeToken: TypeToken<T>
    ): T {
        val bytes = try {
            DataMapItem.fromDataItem(dataItem)
                .dataMap
                .getByteArray(key)
        } catch (e: IllegalArgumentException) {
            Log.e("CellReader", "error", e)
            null
        } catch (e: IllegalStateException) {
            Log.e("CellReader", "error", e)
            null
        } ?: return empty

        val json = bytes.decodeToString()

        return try {
            gson.fromJson<T>(
                json,
                typeToken.type
            ) ?: empty
        } catch (e: Exception) {
            Log.e("CellReader", "error", e)
            empty
        }
    }
}