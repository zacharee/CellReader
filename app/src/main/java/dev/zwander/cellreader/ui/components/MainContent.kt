package dev.zwander.cellreader.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import dev.zwander.cellreader.UpdaterService
import dev.zwander.cellreader.data.data.LocalCellModel
import dev.zwander.cellreader.data.layouts.CellSignalStrengthCard
import dev.zwander.cellreader.data.layouts.SIMCard
import dev.zwander.cellreader.data.layouts.SignalCard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterialApi::class)
@Composable
fun MainContent() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val model = LocalCellModel.current

    val showingCells = remember {
        mutableStateMapOf<Int, Boolean>()
    }
    val expanded = remember {
        mutableStateMapOf<String, Boolean>()
    }

    val refreshing by model.isRefreshing.collectAsState()
    val subIds by model.subIds.collectAsState()
    val cellInfos by model.cellInfos.collectAsState()
    val strengthInfos by model.strengthInfos.collectAsState()

    val refreshState = rememberPullRefreshState(
        refreshing = refreshing,
        onRefresh = {
            scope.launch(Dispatchers.IO) {
                UpdaterService.refresh(context)
            }
        }
    )

    Box(
        modifier = Modifier.pullRefresh(
            state = refreshState
        )
    ) {
        Crossfade(
            targetState = refreshing,
            modifier = Modifier.fillMaxSize()
        ) {
            if (it) {
                Box(modifier = Modifier.fillMaxSize()) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
            } else {
                SelectionContainer(
                    modifier = Modifier.fillMaxSize()
                ) {
                    val state = rememberLazyGridState()

                    LazyVerticalGrid(
                        contentPadding = WindowInsets.systemBars
                            .add(WindowInsets(left = 8.dp, right = 8.dp, top = 8.dp, bottom = 8.dp + 24.dp))
                            .asPaddingValues(),
                        state = state,
                        modifier = Modifier.fillMaxHeight(),
                        columns = GridCells.Adaptive(minSize = 300.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        subIds.forEachIndexed { subIndex, t ->
                            item(t, span = { GridItemSpan(this.maxLineSpan) }) {
                                SIMCard(
                                    subId = t,
                                    expanded = expanded[t.toString()] ?: false,
                                    onExpand = { expanded[t.toString()] = it },
                                    showingCells = showingCells[t] ?: true,
                                    onShowingCells = { showingCells[t] = it },
                                    modifier = Modifier
                                        .animateItemPlacement()
                                        .padding(bottom = 8.dp),
                                )
                            }

                            val lastCellIndex = cellInfos[t]?.lastIndex ?: 0
                            val lastStrengthIndex = strengthInfos[t]?.lastIndex ?: 0
                            val cellInfosEmpty = cellInfos[t]?.isEmpty() ?: true

                            itemsIndexed(strengthInfos[t]!!, { index, _ -> "$t:$index" }) { index, item ->
                                val isFinal = index == lastStrengthIndex && cellInfosEmpty

                                AnimatedVisibility(
                                    visible = showingCells[t] != false,
                                    modifier = Modifier
                                        .animateItemPlacement()
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

                            itemsIndexed(
                                cellInfos[t]!!,
                                { _, item -> "$t:${item.cellIdentity}" }) { index, item ->
                                val isFinal = index == lastCellIndex

                                val key = remember(item.cellIdentity) {
                                    "$t:${item.cellIdentity}"
                                }

                                AnimatedVisibility(
                                    visible = showingCells[t] != false,
                                    modifier = Modifier
                                        .animateItemPlacement()
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

        PullRefreshIndicator(
            refreshing = false,
            state = refreshState,
            modifier = Modifier.align(Alignment.TopCenter),
            backgroundColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.contentColorFor(MaterialTheme.colorScheme.surface)
        )
    }
}