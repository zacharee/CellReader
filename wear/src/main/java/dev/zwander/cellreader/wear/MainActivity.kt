package dev.zwander.cellreader.wear

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.ScalingLazyColumn
import androidx.wear.compose.material.itemsIndexed
import androidx.wear.compose.material.rememberScalingLazyListState
import com.google.android.gms.wearable.*
import dev.zwander.cellreader.data.BetweenUtils
import dev.zwander.cellreader.data.data.CellModel
import dev.zwander.cellreader.data.data.CellModelWear
import dev.zwander.cellreader.data.layouts.CellSignalStrengthCard
import dev.zwander.cellreader.data.layouts.SIMCard
import dev.zwander.cellreader.data.layouts.SignalCard
import kotlinx.coroutines.*
import java.util.*
import kotlin.math.sign

class MainActivity : ComponentActivity(), CoroutineScope by MainScope() {
    private val loadCellInfosJobs = Collections.synchronizedList(arrayListOf<Job>())
    private val loadCellSignalStrengthsJobs = Collections.synchronizedList(arrayListOf<Job>())
    private val loadServiceStateJobs = Collections.synchronizedList(arrayListOf<Job>())
    private val loadSubInfoJobs = Collections.synchronizedList(arrayListOf<Job>())
    private val addSubIdJobs = Collections.synchronizedList(arrayListOf<Job>())
    private var updatePrimaryCellJob: Job? = null

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

    @OptIn(ExperimentalFoundationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        dataClient.addListener(listener)

        setContent {
            val state = rememberScalingLazyListState()

            val showingCells = remember {
                mutableStateMapOf<Int, Boolean>()
            }
            val expanded = remember {
                mutableStateMapOf<String, Boolean>()
            }

            with (CellModelWear) {
                ScalingLazyColumn(
                    state = state,
                    modifier = Modifier.fillMaxHeight()
                ) {
                    sortedSubIds.forEachIndexed { subIndex, t ->
                        item(t) {
                            SIMCard(
                                subInfo = subInfos[t],
                                expanded = expanded[t.toString()] ?: false,
                                onExpand = { expanded[t.toString()] = it },
                                showingCells = showingCells[t] ?: true,
                                onShowingCells = { showingCells[t] = it },
                                modifier = Modifier
                                    .padding(bottom = 8.dp),
                                wear = true,
                            )
                        }

                        val lastCellIndex = cellInfos[t]?.lastIndex ?: 0
                        val lastStrengthIndex = strengthInfos[t]?.lastIndex ?: 0
                        val strengthsEmpty = strengthInfos[t]?.isEmpty() ?: true

                        cellInfos[t]?.let { cellInfo ->
                            itemsIndexed(cellInfo, { _, item -> "$t:${item.cellIdentity}" }) { index, item ->
                                val isFinal = index == lastCellIndex && strengthsEmpty

                                AnimatedVisibility(
                                    visible = showingCells[t] != false,
                                    modifier = Modifier
                                        .padding(bottom = if (!isFinal || subIndex != sortedSubIds.lastIndex) 8.dp else 0.dp),
                                    enter = fadeIn() + expandIn(clip = false, expandFrom = Alignment.TopEnd),
                                    exit = shrinkOut(clip = false, shrinkTowards = Alignment.TopEnd) + fadeOut()
                                ) {
                                    val key = remember(item.cellIdentity) {
                                        "$t:${item.cellIdentity}"
                                    }

                                    SignalCard(
                                        cellInfo = item,
                                        expanded = expanded[key] ?: false,
                                        isFinal = isFinal,
                                        onExpand = { expanded[key] = it },
                                        modifier = Modifier
                                            .fillMaxWidth(),
                                        wear = true
                                    )
                                }
                            }
                        }

                        strengthInfos[t]?.let { strengthInfo ->
                            itemsIndexed(strengthInfo, { index, _ -> "$t:$index" }) { index, item ->
                                val isFinal = index == lastStrengthIndex

                                AnimatedVisibility(
                                    visible = showingCells[t] != false,
                                    modifier = Modifier
                                        .padding(bottom = if (!isFinal || subIndex != sortedSubIds.lastIndex) 8.dp else 0.dp),
                                    enter = fadeIn() + expandIn(clip = false, expandFrom = Alignment.TopEnd),
                                    exit = shrinkOut(clip = false, shrinkTowards = Alignment.TopEnd) + fadeOut()
                                ) {
                                    CellSignalStrengthCard(
                                        cellSignalStrength = item,
                                        isFinal = isFinal,
                                        modifier = Modifier
                                            .fillMaxWidth(),
                                        wear = true
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

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
                    cellInfos.forEach { (t, u) ->
                        CellModelWear.cellInfos[t] = u
                    }
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
                    signalStrengths.forEach { (t, u) ->
                        CellModelWear.strengthInfos[t] = u
                    }
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
                    serviceState.forEach { (t, u) ->
                        CellModelWear.serviceStates[t] = u
                    }
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
                    subInfo.forEach { (t, u) ->
                        CellModelWear.subInfos[t] = u
                    }
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
                    CellModelWear.subIds.clear()
                    CellModelWear.subIds.addAll(subId)
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