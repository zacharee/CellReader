package dev.zwander.cellreader.wear

import android.app.Activity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.google.android.gms.wearable.*
import dev.zwander.cellreader.data.BetweenUtils
import dev.zwander.cellreader.data.data.CellModel
import dev.zwander.cellreader.data.data.CellModelWear
import dev.zwander.cellreader.wear.databinding.ActivityMainBinding
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.util.*

class MainActivity : Activity(), CoroutineScope by MainScope() {
    private val loadCellInfosJobs = Collections.synchronizedList(arrayListOf<Job>())
    private val loadCellSignalStrengthsJobs = Collections.synchronizedList(arrayListOf<Job>())
    private val loadServiceStateJobs = Collections.synchronizedList(arrayListOf<Job>())
    private val loadSubInfoJobs = Collections.synchronizedList(arrayListOf<Job>())
    private val addSubIdJobs = Collections.synchronizedList(arrayListOf<Job>())
    private var updatePrimaryCellJob: Job? = null

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private val betweenUtils by lazy { BetweenUtils.getInstance(this) }
    private val dataClient by lazy { Wearable.getDataClient(this) }

    private val listener = DataClient.OnDataChangedListener {
        it.forEach { event ->
            val frozen = event.dataItem.freeze()

            when (event.dataItem.uri.path) {
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
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)
        dataClient.addListener(listener)

        dataClient.dataItems.addOnCompleteListener { item ->
            item.result.forEach {
                with (it.uri.toString()) {
                    when {
                        contains(BetweenUtils.CELL_INFOS_PATH) -> {
                            updateCellInfos(it.freeze())
                        }
                        contains(BetweenUtils.CELL_SIGNAL_STRENGTHS_PATH) -> {
                            updateSignalStrengths(it.freeze())
                        }
                        contains(BetweenUtils.SERVICE_STATE_PATH) -> {
                            updateServiceState(it.freeze())
                        }
                        contains(BetweenUtils.SUB_INFO_PATH) -> {
                            updateSubInfo(it.freeze())
                        }
                        contains(BetweenUtils.SUB_ID_PATH) -> {
                            addSubId(it.freeze())
                        }
                        contains(BetweenUtils.PRIMARY_CELL_PATH) -> {
                            updatePrimaryCell(it.freeze())
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        cancel()
        dataClient.removeListener(listener)
    }

    private fun updateCellInfos(item: DataItem) {
        loadCellInfosJobs.add(
            launch(Dispatchers.IO) {
                val cellInfos = betweenUtils.retrieveCellInfos(item)

                withContext(Dispatchers.Main) {
                    CellModelWear.cellInfos[cellInfos.first] = cellInfos.second
                }

                coroutineContext[Job]?.let { loadCellInfosJobs.remove(it) }
            }
        )
    }

    private fun updateSignalStrengths(item: DataItem) {
        loadCellSignalStrengthsJobs.add(
            launch(Dispatchers.IO) {
                val signalStrengths = betweenUtils.retrieveSignalStrengths(item)

                withContext(Dispatchers.Main) {
                    CellModelWear.strengthInfos[signalStrengths.first] = signalStrengths.second
                }

                coroutineContext[Job]?.let { loadCellSignalStrengthsJobs.remove(it) }
            }
        )
    }

    private fun updateServiceState(item: DataItem) {
        loadServiceStateJobs.add(
            launch(Dispatchers.IO) {
                val serviceState = betweenUtils.retrieveServiceState(item) ?: return@launch

                withContext(Dispatchers.Main) {
                    CellModelWear.serviceStates[serviceState.first] = serviceState.second
                }

                coroutineContext[Job]?.let { loadServiceStateJobs.remove(it) }
            }
        )
    }

    private fun updateSubInfo(item: DataItem) {
        loadSubInfoJobs.add(
            launch(Dispatchers.IO) {
                val subInfo = betweenUtils.retrieveSubscriptionInfo(item)

                withContext(Dispatchers.Main) {
                    CellModelWear.subInfos[subInfo.first] = subInfo.second
                }

                coroutineContext[Job]?.let { loadSubInfoJobs.remove(it) }
            }
        )
    }

    private fun addSubId(item: DataItem) {
        addSubIdJobs.add(
            launch(Dispatchers.IO) {
                val subId = betweenUtils.retrieveNewSubId(item)

                withContext(Dispatchers.Main) {
                    CellModelWear.subIds.add(subId)
                }

                coroutineContext[Job]?.let { addSubIdJobs.remove(it) }
            }
        )
    }

    private fun updatePrimaryCell(item: DataItem) {
        updatePrimaryCellJob?.cancel()
        updatePrimaryCellJob = launch(Dispatchers.IO) {
            val primaryCell = betweenUtils.retrievePrimaryCell(item)

            withContext(Dispatchers.Main) {
                CellModelWear.primaryCell = primaryCell
            }
        }
    }

    private fun clear() {
        CellModelWear.clear()
    }
}