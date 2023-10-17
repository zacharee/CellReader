package dev.zwander.cellreader.data.layouts

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.zwander.cellreader.data.components.PaddedDivider
import dev.zwander.cellreader.data.data.LocalCellModel
import dev.zwander.cellreader.data.util.SpacedArrangement

@OptIn(ExperimentalLayoutApi::class)
@SuppressLint("MissingPermission")
@Composable
fun AdvancedSubInfo(
    subId: Int,
    scrollState: LazyListState,
    modifier: Modifier = Modifier,
) {
    val subInfos by LocalCellModel.current.subInfos.collectAsState()
    val displayInfos by LocalCellModel.current.displayInfos.collectAsState()
    val signalStrengths = LocalCellModel.current.signalStrengths?.collectAsState()
    val serviceStates by LocalCellModel.current.serviceStates.collectAsState()

    val signalStrength = signalStrengths?.value?.get(subId)

    Box(
        modifier = modifier
    ) {
        ProvideTextStyle(value = LocalTextStyle.current.copy(textAlign = TextAlign.Center)) {
            LazyColumn(
                state = scrollState
            ) {
                displayInfos[subId]?.let {
                    item {
                        FlowRow(
                            horizontalArrangement = SpacedArrangement(
                                spacing = 16.dp,
                                arrangement = Arrangement.SpaceEvenly,
                            ),
                        ) {
                            DisplayInfo(info = it)
                        }
                    }
                }

                item {
                    PaddedDivider(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp)
                    )
                }

                signalStrength?.let {
                    item {
                        FlowRow(
                            horizontalArrangement = SpacedArrangement(
                                spacing = 16.dp,
                                arrangement = Arrangement.SpaceEvenly,
                            ),
                        ) {
                            SignalStrength(signalStrength = it)
                        }
                    }
                }

                item {
                    PaddedDivider(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp)
                    )
                }

                serviceStates[subId]?.let {
                    item {
                        FlowRow(
                            horizontalArrangement = SpacedArrangement(
                                spacing = 16.dp,
                                arrangement = Arrangement.SpaceEvenly,
                            ),
                        ) {
                            ServiceState(serviceState = it)
                        }
                    }
                }

                subInfos[subId]?.let {
                    item {
                        FlowRow(
                            horizontalArrangement = SpacedArrangement(
                                spacing = 16.dp,
                                arrangement = Arrangement.SpaceEvenly,
                            ),
                        ) {
                            SubInfo(subscriptionInfo = it)
                        }
                    }
                }
            }
        }
    }
}