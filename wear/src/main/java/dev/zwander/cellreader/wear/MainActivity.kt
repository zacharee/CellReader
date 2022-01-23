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

class MainActivity : Activity(), CoroutineScope by MainScope() {
    private var loadCellInfosJob: Job? = null
    private var loadCellSignalStrengthsJob: Job? = null
    private var loadServiceStateJob: Job? = null

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
        loadCellInfosJob?.cancel()
        loadCellInfosJob = launch(Dispatchers.IO) {
            val cellInfos = betweenUtils.retrieveCellInfos(item)

            CellModelWear.cellInfos[cellInfos.first] = cellInfos.second
        }
    }

    private fun updateSignalStrengths(item: DataItem) {
        loadCellSignalStrengthsJob?.cancel()
        loadCellSignalStrengthsJob = launch(Dispatchers.IO) {
            val signalStrengths = betweenUtils.retrieveSignalStrengths(item)

            CellModelWear.strengthInfos[signalStrengths.first] = signalStrengths.second
        }
    }

    private fun updateServiceState(item: DataItem) {
        loadServiceStateJob?.cancel()
        loadServiceStateJob = launch(Dispatchers.IO) {
            val serviceState = betweenUtils.retrieveServiceState(item) ?: return@launch

            CellModelWear.serviceStates[serviceState.first] = serviceState.second
        }
    }
}