package dev.zwander.cellreader.ui.components

import android.annotation.SuppressLint
import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
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

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterialApi::class)
@Composable
fun MainContent() {
    val context = LocalContext.current
    val model = LocalCellModel.current

    val showingCells = remember {
        mutableStateMapOf<Int, Boolean>()
    }
    val expanded = remember {
        mutableStateMapOf<String, Boolean>()
    }

    val refreshing by model.isRefreshing.collectAsState()

    val actualSubIdsState by model.subIds.collectAsState()
    val actualCellInfosState by model.cellInfos.collectAsState()
    val actualStrengthInfosState by model.strengthInfos.collectAsState()

    @SuppressLint("MutableCollectionMutableState")
    var subIds by remember {
        mutableStateOf(actualSubIdsState)
    }

    @SuppressLint("MutableCollectionMutableState")
    var cellInfos by remember {
        mutableStateOf(actualCellInfosState)
    }

    @SuppressLint("MutableCollectionMutableState")
    var strengthInfos by remember {
        mutableStateOf(actualStrengthInfosState)
    }

    val refreshState = rememberPullRefreshState(
        refreshing = refreshing,
        onRefresh = {
            UpdaterService.refresh(context)
        }
    )

    LaunchedEffect(
        actualSubIdsState.toList(),
        actualCellInfosState.toList(),
        actualStrengthInfosState.toList()
    ) {
        if (actualSubIdsState.isNotEmpty()) {
            subIds = actualSubIdsState
            cellInfos = actualCellInfosState
            strengthInfos = actualStrengthInfosState
        }
    }

    Box(
        modifier = Modifier.pullRefresh(
            state = refreshState
        )
    ) {
        SelectionContainer(
            modifier = Modifier.fillMaxSize()
        ) {
            val state = rememberLazyStaggeredGridState()

            Crossfade(
                targetState = actualSubIdsState.isNotEmpty() && !refreshing,
                label = "MainGrid"
            ) { hasSubIds ->
                if (hasSubIds) {
                    LazyVerticalStaggeredGrid(
                        contentPadding = WindowInsets.systemBars
                            .add(
                                WindowInsets(
                                    left = 8.dp,
                                    right = 8.dp,
                                    top = 8.dp,
                                    bottom = 8.dp + 24.dp
                                )
                            )
                            .asPaddingValues(),
                        state = state,
                        modifier = Modifier.fillMaxHeight(),
                        columns = StaggeredGridCells.Adaptive(minSize = 300.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        subIds.forEachIndexed { subIndex, t ->
                            item(t, span = StaggeredGridItemSpan.FullLine ) {
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

                            itemsIndexed(
                                strengthInfos[t]!!,
                                { index, _ -> "$t:$index" }) { index, item ->
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
                } else {
                    Box(modifier = Modifier.fillMaxSize()) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
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