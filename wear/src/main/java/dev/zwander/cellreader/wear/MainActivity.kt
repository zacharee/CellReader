package dev.zwander.cellreader.wear

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.ScalingLazyColumn
import androidx.wear.compose.material.Vignette
import androidx.wear.compose.material.VignettePosition
import androidx.wear.compose.material.itemsIndexed
import androidx.wear.compose.material.rememberScalingLazyListState
import dev.zwander.cellreader.data.CellReaderTheme
import dev.zwander.cellreader.data.data.CellModelWear
import dev.zwander.cellreader.data.layouts.CellSignalStrengthCard
import dev.zwander.cellreader.data.layouts.SIMCard
import dev.zwander.cellreader.data.layouts.SignalCard
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity(), CoroutineScope by MainScope() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        launch {
            DataHandler.addHandle(this@MainActivity, this@MainActivity)
        }

        setContent {
            CellReaderTheme {
                CellModelWear.ProvideCellModel {
                    MainContent()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        launch {
            DataHandler.removeHandle(this@MainActivity, this@MainActivity)

            this@MainActivity.cancel()
        }
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

    val subIds by CellModelWear.subIds.collectAsState()
    val cellInfos by CellModelWear.cellInfos.collectAsState()
    val strengthInfos by CellModelWear.strengthInfos.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        vignette = {
            Vignette(vignettePosition = VignettePosition.TopAndBottom)
        },
    ) {
        Crossfade(targetState = subIds.isEmpty()) {
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
                        subIds.forEachIndexed { subIndex, t ->
                            item(t) {
                                SIMCard(
                                    subId = t,
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
                            val cellInfosEmpty = cellInfos[t]?.isEmpty() ?: true

                            strengthInfos[t]?.let { strengthInfo ->
                                itemsIndexed(strengthInfo, { index, _ -> "$t:$index" }) { index, item ->
                                    val isFinal = index == lastStrengthIndex && cellInfosEmpty

                                    AnimatedVisibility(
                                        visible = showingCells[t] != false,
                                        modifier = Modifier
                                            .padding(bottom = if (!isFinal || subIndex != subIds.size - 1) 8.dp else 0.dp),
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
                                                .fillMaxWidth()
                                        )
                                    }
                                }
                            }

                            cellInfos[t]?.let { cellInfo ->
                                itemsIndexed(
                                    cellInfo,
                                    { _, item -> "$t:${item.cellIdentity}" }) { index, item ->
                                    val isFinal = index == lastCellIndex

                                    AnimatedVisibility(
                                        visible = showingCells[t] != false,
                                        modifier = Modifier
                                            .padding(bottom = if (!isFinal || subIndex != subIds.size - 1) 8.dp else 0.dp),
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
                                            isFinal = isFinal,
                                            expanded = expanded[key] ?: false,
                                            onExpand = { expanded[key] = it },
                                            modifier = Modifier
                                                .fillMaxWidth()
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