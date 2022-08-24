package dev.zwander.cellreader.data.layouts

import android.annotation.SuppressLint
import android.telephony.SignalStrength
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.accompanist.flowlayout.FlowRow
import com.google.accompanist.flowlayout.MainAxisAlignment
import com.google.accompanist.flowlayout.SizeMode
import dev.zwander.cellreader.data.components.PaddedDivider
import dev.zwander.cellreader.data.data.LocalCellModel
import dev.zwander.cellreader.data.wrappers.ServiceStateWrapper
import dev.zwander.cellreader.data.wrappers.SubscriptionInfoWrapper
import dev.zwander.cellreader.data.wrappers.TelephonyDisplayInfoWrapper

@SuppressLint("MissingPermission")
@Composable
fun AdvancedSubInfo(
    subId: Int,
    scrollState: LazyListState,
    modifier: Modifier = Modifier,
) {
    val subInfos by LocalCellModel.current.LocalSubInfos.current.observeAsState()
    val displayInfos by LocalCellModel.current.LocalDisplayInfos.current.observeAsState()
    val signalStrengths = LocalCellModel.current.LocalSignalStrengths.current?.observeAsState()
    val serviceStates by LocalCellModel.current.LocalServiceStates.current.observeAsState()

    val signalStrength = signalStrengths?.value?.get(subId)

    Box(
        modifier = modifier
    ) {
        ProvideTextStyle(value = LocalTextStyle.current.copy(textAlign = TextAlign.Center)) {
            LazyColumn(
                state = scrollState
            ) {
                displayInfos!![subId]?.let {
                    item {
                        FlowRow(
                            mainAxisSpacing = 16.dp,
                            mainAxisAlignment = MainAxisAlignment.SpaceEvenly,
                            mainAxisSize = SizeMode.Expand
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
                            mainAxisSpacing = 16.dp,
                            mainAxisAlignment = MainAxisAlignment.SpaceEvenly,
                            mainAxisSize = SizeMode.Expand
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

                serviceStates!![subId]?.let {
                    item {
                        FlowRow(
                            mainAxisSpacing = 16.dp,
                            mainAxisAlignment = MainAxisAlignment.SpaceEvenly,
                            mainAxisSize = SizeMode.Expand
                        ) {
                            ServiceState(serviceState = it)
                        }
                    }
                }

                subInfos!![subId]?.let {
                    item {
                        FlowRow(
                            mainAxisSpacing = 16.dp,
                            mainAxisAlignment = MainAxisAlignment.SpaceEvenly,
                            mainAxisSize = SizeMode.Expand
                        ) {
                            SubInfo(subscriptionInfo = it)
                        }
                    }
                }
            }
        }
    }
}