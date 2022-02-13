package dev.zwander.cellreader.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.rememberInsetsPaddingValues
import dev.zwander.cellreader.data.data.CellModel
import dev.zwander.cellreader.data.layouts.CellSignalStrengthCard
import dev.zwander.cellreader.data.layouts.SIMCard
import dev.zwander.cellreader.data.layouts.SignalCard

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainContent() {
    val showingCells = remember {
        mutableStateMapOf<Int, Boolean>()
    }
    val expanded = remember {
        mutableStateMapOf<String, Boolean>()
    }

    with (CellModel) {
        SelectionContainer {
            val state = rememberLazyListState()

            LazyColumn(
                contentPadding = rememberInsetsPaddingValues(
                    insets = LocalWindowInsets.current.systemBars,
                    applyTop = true,
                    applyBottom = true,
                    additionalStart = 8.dp,
                    additionalTop = 8.dp,
                    additionalEnd = 8.dp,
                    additionalBottom = 8.dp + 24.dp
                ),
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
                                .animateItemPlacement()
                                .padding(bottom = 8.dp),
                            signalStrength = signalStrengths[t]
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
                                .padding(bottom = if (!isFinal || subIndex != sortedSubIds.lastIndex) 8.dp else 0.dp),
                            enter = fadeIn() + expandIn(clip = false, expandFrom = Alignment.TopEnd),
                            exit = shrinkOut(clip = false, shrinkTowards = Alignment.TopEnd) + fadeOut()
                        ) {
                            CellSignalStrengthCard(
                                cellSignalStrength = item,
                                isFinal = isFinal,
                                modifier = Modifier
                                    .fillMaxWidth()
                            )
                        }
                    }

                    itemsIndexed(cellInfos[t]!!, { _, item -> "$t:${item.cellIdentity}" }) { index, item ->
                        val isFinal = index == lastCellIndex

                        val key = remember(item.cellIdentity) {
                            "$t:${item.cellIdentity}"
                        }

                        AnimatedVisibility(
                            visible = showingCells[t] != false,
                            modifier = Modifier
                                .animateItemPlacement()
                                .padding(bottom = if (!isFinal || subIndex != sortedSubIds.lastIndex) 8.dp else 0.dp),
                            enter = fadeIn() + expandIn(clip = false, expandFrom = Alignment.TopEnd),
                            exit = shrinkOut(clip = false, shrinkTowards = Alignment.TopEnd) + fadeOut()
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