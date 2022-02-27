package dev.zwander.cellreader.data.layouts

import android.annotation.SuppressLint
import android.telephony.SignalStrength
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.accompanist.flowlayout.FlowRow
import com.google.accompanist.flowlayout.MainAxisAlignment
import com.google.accompanist.flowlayout.SizeMode
import dev.zwander.cellreader.data.components.CarouselScrollState
import dev.zwander.cellreader.data.components.PaddedDivider
import dev.zwander.cellreader.data.wrappers.ServiceStateWrapper
import dev.zwander.cellreader.data.wrappers.SubscriptionInfoWrapper

@SuppressLint("MissingPermission")
@Composable
fun AdvancedSubInfo(
    subId: Int,
    scrollState: LazyListState,
    modifier: Modifier = Modifier,
    serviceStates: Map<Int, ServiceStateWrapper?>,
    subInfos: Map<Int, SubscriptionInfoWrapper?>,
    signalStrength: SignalStrength? = null,
) {
    Box(
        modifier = modifier
    ) {
        ProvideTextStyle(value = LocalTextStyle.current.copy(textAlign = TextAlign.Center)) {
            LazyColumn(
                state = scrollState
            ) {
                item {
                    FlowRow(
                        mainAxisSpacing = 16.dp,
                        mainAxisAlignment = MainAxisAlignment.SpaceEvenly,
                        mainAxisSize = SizeMode.Expand
                    ) {
                        signalStrength?.apply {
                            SignalStrength(signalStrength = this)
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

                item {
                    FlowRow(
                        mainAxisSpacing = 16.dp,
                        mainAxisAlignment = MainAxisAlignment.SpaceEvenly,
                        mainAxisSize = SizeMode.Expand
                    ) {
                        serviceStates[subId]?.apply {
                            ServiceState(serviceState = this)
                        }

                        subInfos[subId]?.apply {
                            SubInfo(subscriptionInfo = this)
                        }
                    }
                }

                item {
                    FlowRow(
                        mainAxisSpacing = 16.dp,
                        mainAxisAlignment = MainAxisAlignment.SpaceEvenly,
                        mainAxisSize = SizeMode.Expand
                    ) {
                        subInfos[subId]?.apply {
                            SubInfo(subscriptionInfo = this)
                        }
                    }
                }
            }


        }
    }
}