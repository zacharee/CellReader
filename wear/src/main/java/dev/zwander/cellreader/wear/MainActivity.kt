package dev.zwander.cellreader.wear

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.*
import com.google.android.gms.wearable.*
import dev.zwander.cellreader.data.BetweenUtils
import dev.zwander.cellreader.data.data.CellModelWear
import dev.zwander.cellreader.data.layouts.CellSignalStrengthCard
import dev.zwander.cellreader.data.layouts.SIMCard
import dev.zwander.cellreader.data.layouts.SignalCard
import kotlinx.coroutines.*

class MainActivity : ComponentActivity(), CoroutineScope by MainScope() {
    private var loadCellInfosJob: Job? = null
    private var loadCellSignalStrengthsJob: Job? = null
    private var loadServiceStateJob: Job? = null
    private var loadSubInfoJob: Job? = null
    private var addSubIdJob: Job? = null
    private var updatePrimaryCellJob: Job? = null

    private val betweenUtils by lazy { BetweenUtils.getInstance(this) }
    private val dataClient by lazy { Wearable.getDataClient(this) }

    private val listener = DataClient.OnDataChangedListener {
        it.firstOrNull { event ->
            event.dataItem.uri.path == BetweenUtils.CLEAR_PATH
        }?.let { event ->
            handleDataItem(event.dataItem.freeze())
        }

        it.forEach { event ->
            if (event.dataItem.uri.path != BetweenUtils.CLEAR_PATH) {
                val frozen = event.dataItem.freeze()

                handleDataItem(frozen)
            }
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        dataClient.addListener(listener)

        setContent {
            CellReaderTheme {
                CompositionLocalProvider(
                    androidx.compose.material.LocalTextStyle provides LocalTextStyle.current,
                    androidx.compose.material.LocalContentAlpha provides LocalContentAlpha.current,
                    androidx.compose.material.LocalContentColor provides LocalContentColor.current
                ) {
                    MainContent()
                }
            }
        }

        dataClient.dataItems.addOnCompleteListener { item ->
            launch(Dispatchers.IO) {
                item.result.firstOrNull {
                    it.uri.path == BetweenUtils.CLEAR_PATH
                }?.let {
                    handleDataItem(it.freeze())
                }

                item.result.forEach {
                    if (it.uri.path != BetweenUtils.CLEAR_PATH) {
                        handleDataItem(it.freeze())
                    }
                }

                item.result.release()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        cancel()
        dataClient.removeListener(listener)
    }

    private fun handleDataItem(frozen: DataItem) {
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
        }
    }

    private fun updateCellInfos(item: DataItem) {
        loadCellInfosJob?.cancel()
        loadCellInfosJob = launch(Dispatchers.IO) {
            val cellInfos = betweenUtils.retrieveCellInfos(item)

            withContext(Dispatchers.Main) {
                CellModelWear.cellInfos.putAll(cellInfos)
            }
        }
    }

    private fun updateSignalStrengths(item: DataItem) {
        loadCellSignalStrengthsJob?.cancel()
        loadCellSignalStrengthsJob = launch(Dispatchers.IO) {
            val signalStrengths = betweenUtils.retrieveSignalStrengths(item)

            withContext(Dispatchers.Main) {
                CellModelWear.strengthInfos.putAll(signalStrengths)
            }
        }
    }

    private fun updateServiceState(item: DataItem) {
        loadServiceStateJob?.cancel()
        loadServiceStateJob = launch(Dispatchers.IO) {
            val serviceState = betweenUtils.retrieveServiceState(item)

            withContext(Dispatchers.Main) {
                CellModelWear.serviceStates.putAll(serviceState)
            }
        }
    }

    private fun updateSubInfo(item: DataItem) {
        loadSubInfoJob?.cancel()
        loadSubInfoJob = launch(Dispatchers.IO) {
            val subInfo = betweenUtils.retrieveSubscriptionInfo(item)

            withContext(Dispatchers.Main) {
                CellModelWear.subInfos.putAll(subInfo)
            }
        }
    }

    private fun addSubId(item: DataItem) {
        addSubIdJob?.cancel()
        addSubIdJob = launch(Dispatchers.IO) {
            val subId = betweenUtils.retrieveNewSubId(item)

            withContext(Dispatchers.Main) {
                CellModelWear.subIds.clear()
                CellModelWear.subIds.addAll(subId)
            }
        }
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

@Composable
fun MainContent() {
    val state = rememberScalingLazyListState()

    val showingCells = remember {
        mutableStateMapOf<Int, Boolean>()
    }
    val expanded = remember {
        mutableStateMapOf<String, Boolean>()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        vignette = {
            Vignette(vignettePosition = VignettePosition.TopAndBottom)
        },
    ) {
        with(CellModelWear) {
            Crossfade(targetState = sortedSubIds.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (it) {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else {
                        ScalingLazyColumn(
                            state = state,
                            modifier = Modifier.fillMaxHeight(),
                            verticalArrangement = Arrangement.spacedBy(0.dp)
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
                                    )
                                }

                                val lastCellIndex = cellInfos[t]?.lastIndex ?: 0
                                val lastStrengthIndex = strengthInfos[t]?.lastIndex ?: 0
                                val strengthsEmpty = strengthInfos[t]?.isEmpty() ?: true

                                cellInfos[t]?.let { cellInfo ->
                                    itemsIndexed(
                                        cellInfo,
                                        { _, item -> "$t:${item.cellIdentity}" }) { index, item ->
                                        val isFinal = index == lastCellIndex && strengthsEmpty

                                        AnimatedVisibility(
                                            visible = showingCells[t] != false,
                                            modifier = Modifier
                                                .padding(bottom = if (!isFinal || subIndex != sortedSubIds.lastIndex) 8.dp else 0.dp),
                                            enter = fadeIn() + expandIn(
                                                clip = false,
                                                expandFrom = Alignment.TopEnd
                                            ),
                                            exit = shrinkOut(
                                                clip = false,
                                                shrinkTowards = Alignment.TopEnd
                                            ) + fadeOut()
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
                                            enter = fadeIn() + expandIn(
                                                clip = false,
                                                expandFrom = Alignment.TopEnd
                                            ),
                                            exit = shrinkOut(
                                                clip = false,
                                                shrinkTowards = Alignment.TopEnd
                                            ) + fadeOut()
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
            }
        }
    }
}