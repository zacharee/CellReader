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
import dev.zwander.cellreader.data.ProvideCellModel
import dev.zwander.cellreader.ui.layouts.CellSignalStrengthCard
import dev.zwander.cellreader.ui.layouts.SIMCard
import dev.zwander.cellreader.ui.layouts.SignalCard
import dev.zwander.cellreader.utils.cellIdentityCompat

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainContent() {
    val showingCells = remember {
        mutableStateMapOf<Int, Boolean>()
    }
    val expanded = remember {
        mutableStateMapOf<String, Boolean>()
    }

    ProvideCellModel {
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
                            telephony = telephonies[t]!!,
                            subInfo = subInfos[t],
                            expanded = expanded[t.toString()] ?: false,
                            onExpand = { expanded[t.toString()] = it },
                            showingCells = showingCells[t] ?: true,
                            onShowingCells = { showingCells[t] = it },
                            modifier = Modifier
                                .animateItemPlacement()
                                .padding(bottom = 8.dp)
                        )
                    }

                    val lastCellIndex = cellInfos[t]!!.lastIndex
                    val lastStrengthIndex = strengthInfos[t]!!.lastIndex
                    val strengthsEmpty = strengthInfos[t]!!.isEmpty()

                    itemsIndexed(cellInfos[t]!!, { _, item -> "$t:${item.cellIdentityCompat}" }) { index, item ->
                        val isFinal = index == lastCellIndex && strengthsEmpty

                        AnimatedVisibility(
                            visible = showingCells[t] != false,
                            modifier = Modifier
                                .animateItemPlacement()
                                .padding(bottom = if (!isFinal || subIndex != sortedSubIds.lastIndex) 8.dp else 0.dp),
                            enter = fadeIn() + expandIn(clip = false, expandFrom = Alignment.TopEnd),
                            exit = shrinkOut(clip = false, shrinkTowards = Alignment.TopEnd) + fadeOut()
                        ) {
                            val key = remember(item.cellIdentityCompat) {
                                "$t:${item.cellIdentityCompat}"
                            }

                            SignalCard(
                                cellInfo = item,
                                expanded = expanded[key] ?: false,
                                isFinal = isFinal,
                                onExpand = { expanded[key] = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                            )
                        }
                    }

                    itemsIndexed(strengthInfos[t]!!, { index, _ -> "$t:$index" }) { index, item ->
                        val isFinal = index == lastStrengthIndex

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
                }
            }
        }
    }
}