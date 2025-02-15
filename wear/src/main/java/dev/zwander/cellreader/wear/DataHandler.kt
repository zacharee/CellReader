package dev.zwander.cellreader.wear

import android.annotation.SuppressLint
import android.content.Context
import androidx.wear.tiles.TileService
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataItem
import com.google.android.gms.wearable.Wearable
import dev.zwander.cellreader.data.BetweenUtils
import dev.zwander.cellreader.data.SubsComparator
import dev.zwander.cellreader.data.data.CellModelWear
import dev.zwander.cellreader.data.data.DataConnectionState
import dev.zwander.cellreader.data.util.update
import dev.zwander.cellreader.data.wrappers.*
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.tasks.await
import kotlin.collections.ArrayList

class DataHandler private constructor(private val context: Context) {
    interface IDataHandler {
        fun onNewCellInfos(cellInfos: Map<Int, List<CellInfoWrapper>>)
        fun onNewSignalStrengths(strengths: Map<Int, List<CellSignalStrengthWrapper>>)
        fun onNewServiceStates(states: Map<Int, ServiceStateWrapper?>)
        fun onNewSubInfos(infos: Map<Int, SubscriptionInfoWrapper?>)
        fun onNewSubIds(subIds: Collection<Int>)
        fun onNewPrimaryCell(primaryCell: Int)
        fun onNewDataConnectionStates(states: Map<Int, DataConnectionState>)
        fun onNewDisplayInfos(infos: Map<Int, TelephonyDisplayInfoWrapper?>)
        fun onClear()
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        private var instance: DataHandler? = null

        private fun getInstance(context: Context): DataHandler {
            val ctx = context.applicationContext ?: context

            return instance ?: DataHandler(ctx).apply {
                instance = this
            }
        }

        suspend fun addHandle(context: Context, handle: Any) {
            getInstance(context).addHandle(handle)
        }

        suspend fun removeHandle(context: Context, handle: Any) {
            getInstance(context).removeHandle(handle)
        }
    }

    private var loadCellInfosJob: Job? = null
    private var loadCellSignalStrengthsJob: Job? = null
    private var loadServiceStateJob: Job? = null
    private var loadSubInfoJob: Job? = null
    private var addSubIdJob: Job? = null
    private var updatePrimaryCellJob: Job? = null
    private var updateDataConnectionStateJob: Job? = null
    private var updateDisplayInfosJob: Job? = null

    private var listening = false

    private val betweenUtils by lazy { BetweenUtils.getInstance(context) }
    private val dataClient by lazy { Wearable.getDataClient(context) }

    private val handleMutex = Mutex()
    private val handles = ArrayList<Any>()

    private val listenerScope = CoroutineScope(Dispatchers.IO)

    @SuppressLint("VisibleForTests")
    private val listener = DataClient.OnDataChangedListener {
        it.firstOrNull { event ->
            event.dataItem.uri.path == BetweenUtils.CLEAR_PATH
        }?.let { event ->
            listenerScope.launch {
                handleDataItem(event.dataItem.freeze())
            }
        }

        it.forEach { event ->
            if (event.dataItem.uri.path != BetweenUtils.CLEAR_PATH) {
                val frozen = event.dataItem.freeze()

                listenerScope.launch {
                    handleDataItem(frozen)
                }
            }
        }
    }

    suspend fun addHandle(handle: Any) {
        handleMutex.withLock {
            handles.add(handle)

            updateListenerState()
        }
    }

    suspend fun removeHandle(handle: Any) {
        handleMutex.withLock {
            handles.remove(handle)

            updateListenerState()
        }
    }

    private suspend fun updateListenerState() {
        if (handles.isNotEmpty()) {
            if (!listening) {
                dataClient.addListener(listener)
                retrieveInitialData()
                listening = true
            }
        } else {
            dataClient.removeListener(listener)
            listening = false
        }
    }

    @SuppressLint("VisibleForTests")
    private suspend fun retrieveInitialData() {
        val item = dataClient.dataItems.await()

        item.firstOrNull {
            it.uri.path == BetweenUtils.CLEAR_PATH
        }?.let {
            handleDataItem(it.freeze())
        }

        item.forEach {
            if (it.uri.path != BetweenUtils.CLEAR_PATH) {
                handleDataItem(it.freeze())
            }
        }

        item.release()
    }

    @SuppressLint("VisibleForTests")
    private suspend fun handleDataItem(frozen: DataItem) {
        when (frozen.uri.path) {
            BetweenUtils.CELL_INFOS_PATH -> {
                updateCellInfos(frozen)
            }
            BetweenUtils.CELL_SIGNAL_STRENGTHS_PATH -> {
                updateSignalStrengths(frozen)
            }
            BetweenUtils.SERVICE_STATE_PATH -> {
                updateServiceState(frozen)
            }
            BetweenUtils.SUB_INFO_PATH -> {
                updateSubInfo(frozen)
            }
            BetweenUtils.SUB_ID_PATH -> {
                addSubId(frozen)
            }
            BetweenUtils.CLEAR_PATH -> {
                clear()
            }
            BetweenUtils.PRIMARY_CELL_PATH -> {
                updatePrimaryCell(frozen)
            }
            BetweenUtils.DATA_CONNECTION_STATE_PATH -> {
                updateDataConnectionStates(frozen)
            }
            BetweenUtils.DISPLAY_INFOS_PATH -> {
                updateDisplayInfos(frozen)
            }
        }
    }

    private fun updateHandlers(path: String?) {
        val callback: ((IDataHandler) -> Unit) = {
            when (path) {
                BetweenUtils.CELL_INFOS_PATH -> {
                    it.onNewCellInfos(CellModelWear.getInstance().cellInfos.value)
                }
                BetweenUtils.CELL_SIGNAL_STRENGTHS_PATH -> {
                    it.onNewSignalStrengths(CellModelWear.getInstance().strengthInfos.value)
                }
                BetweenUtils.SERVICE_STATE_PATH -> {
                    it.onNewServiceStates(CellModelWear.getInstance().serviceStates.value)
                }
                BetweenUtils.SUB_INFO_PATH -> {
                    it.onNewSubInfos(CellModelWear.getInstance().subInfos.value)
                }
                BetweenUtils.SUB_ID_PATH -> {
                    it.onNewSubIds(CellModelWear.getInstance().subIds.value)
                }
                BetweenUtils.CLEAR_PATH -> {
                    it.onClear()
                }
                BetweenUtils.PRIMARY_CELL_PATH -> {
                    it.onNewPrimaryCell(CellModelWear.getInstance().primaryCell.value)
                }
                BetweenUtils.DATA_CONNECTION_STATE_PATH -> {
                    it.onNewDataConnectionStates(CellModelWear.getInstance().dataConnectionStates.value)
                }
                BetweenUtils.DISPLAY_INFOS_PATH -> {
                    it.onNewDisplayInfos(CellModelWear.getInstance().displayInfos.value)
                }
            }
        }

        handles.forEach {
            if (it is IDataHandler) {
                callback(it)
            }
        }
    }

    @SuppressLint("VisibleForTests")
    private suspend fun updateCellInfos(item: DataItem) = coroutineScope {
        loadCellInfosJob?.cancel()
        loadCellInfosJob = async(Dispatchers.IO) {
            val cellInfos = betweenUtils.retrieveCellInfos(item)

            withContext(Dispatchers.Main) {
                CellModelWear.getInstance().cellInfos.update {
                    it.putAll(cellInfos)
                }
                updateHandlers(item.uri.path)
                updateTiles()
            }
        }
    }

    @SuppressLint("VisibleForTests")
    private suspend fun updateSignalStrengths(item: DataItem) = coroutineScope {
        loadCellSignalStrengthsJob?.cancel()
        loadCellSignalStrengthsJob = async(Dispatchers.IO) {
            val signalStrengths = betweenUtils.retrieveSignalStrengths(item)

            withContext(Dispatchers.Main) {
                CellModelWear.getInstance().strengthInfos.update {
                    it.putAll(signalStrengths)
                }
                updateHandlers(item.uri.path)
                updateTiles()
            }
        }
    }

    @SuppressLint("VisibleForTests")
    private suspend fun updateServiceState(item: DataItem) = coroutineScope {
        loadServiceStateJob?.cancel()
        loadServiceStateJob = async(Dispatchers.IO) {
            val serviceState = betweenUtils.retrieveServiceState(item)

            withContext(Dispatchers.Main) {
                CellModelWear.getInstance().serviceStates.update {
                    it.putAll(serviceState)
                }
                updateHandlers(item.uri.path)
                updateTiles()
            }
        }
    }

    @SuppressLint("VisibleForTests")
    private suspend fun updateSubInfo(item: DataItem) = coroutineScope {
        loadSubInfoJob?.cancel()
        loadSubInfoJob = async(Dispatchers.IO) {
            val subInfos = betweenUtils.retrieveSubscriptionInfo(item)

            withContext(Dispatchers.Main) {
                CellModelWear.getInstance().subInfos.update {
                    it.putAll(subInfos)
                }
                updateHandlers(item.uri.path)
                updateTiles()
            }
        }
    }

    @SuppressLint("VisibleForTests")
    private suspend fun addSubId(item: DataItem) = coroutineScope {
        addSubIdJob?.cancel()
        addSubIdJob = async(Dispatchers.IO) {
            val subIds = betweenUtils.retrieveNewSubId(item)

            withContext(Dispatchers.Main) {
                CellModelWear.getInstance().subIds.update {
                    it.addAll(subIds)
                    it.updateComparator(SubsComparator(CellModelWear.getInstance().primaryCell.value))
                }
                updateHandlers(item.uri.path)
                updateTiles()
            }
        }
    }

    @SuppressLint("VisibleForTests")
    private suspend fun updatePrimaryCell(item: DataItem) = coroutineScope {
        updatePrimaryCellJob?.cancel()
        updatePrimaryCellJob = async(Dispatchers.IO) {
            val primaryCell = betweenUtils.retrievePrimaryCell(item)

            withContext(Dispatchers.Main) {
                CellModelWear.getInstance().primaryCell.value = primaryCell
                updateHandlers(item.uri.path)
                updateTiles()
            }
        }
    }

    @SuppressLint("VisibleForTests")
    private suspend fun updateDataConnectionStates(item: DataItem) = coroutineScope {
        updateDataConnectionStateJob?.cancel()
        updateDataConnectionStateJob = async(Dispatchers.IO) {
            val dataConnectionStates = betweenUtils.retrieveDataConnectionStates(item)

            withContext(Dispatchers.Main) {
                CellModelWear.getInstance().dataConnectionStates.value = dataConnectionStates
                updateHandlers(item.uri.path)
                updateTiles()
            }
        }
    }

    @SuppressLint("VisibleForTests")
    private suspend fun updateDisplayInfos(item: DataItem) = coroutineScope {
        updateDisplayInfosJob?.cancel()
        updateDisplayInfosJob = async(Dispatchers.IO) {
            val displayInfos = betweenUtils.retrieveDisplayInfos(item)

            withContext(Dispatchers.Main) {
                CellModelWear.getInstance().displayInfos.value = displayInfos
                updateHandlers(item.uri.path)
                updateTiles()
            }
        }
    }

    private fun clear() {
        CellModelWear.getInstance().clear()
        updateHandlers(BetweenUtils.CLEAR_PATH)
        updateTiles()
    }

    private fun updateTiles() {
        TileService.getUpdater(context)
            .requestUpdate(CellTile::class.java)
    }
}