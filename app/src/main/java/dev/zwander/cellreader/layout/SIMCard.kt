package dev.zwander.cellreader.layout

import android.annotation.SuppressLint
import android.telephony.*
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.google.accompanist.flowlayout.FlowMainAxisAlignment
import com.google.accompanist.flowlayout.FlowRow
import dev.zwander.cellreader.R
import dev.zwander.cellreader.utils.FormatText
import dev.zwander.cellreader.utils.angledGradient
import dev.zwander.cellreader.utils.asMccMnc

@SuppressLint("MissingPermission")
@Composable
fun SIMCard(
    telephony: TelephonyManager,
    subs: SubscriptionManager,
    subInfo: SubscriptionInfo,
    expanded: Boolean,
    onExpand: (Boolean) -> Unit,
    showingCells: Boolean,
    onShowingCells: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Card(
        modifier = modifier,
        backgroundColor = Color.Transparent
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .angledGradient(
                    listOf(
                        0.0f to colorResource(id = R.color.sim_card),
                        1.0f to colorResource(id = R.color.sim_card_1)
                    ),
                    87f
                )
//                .clickable {
//                    onExpand(!expanded)
//                }
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column {
                val properInfo = telephony.serviceState
                    ?.getNetworkRegistrationInfoListForTransportType(
                        AccessNetworkConstants.TRANSPORT_TYPE_WWAN
                    )
                    ?.first { it.accessNetworkTechnology != TelephonyManager.NETWORK_TYPE_IWLAN }

                FlowRow(
                    mainAxisSpacing = 16.dp,
                    mainAxisAlignment = FlowMainAxisAlignment.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    subInfo.createIconBitmap(context)?.asImageBitmap()?.let {
                        Image(bitmap = it, contentDescription = null)
                    }
                    Text(text = "${subInfo.carrierName}")
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { onShowingCells(!showingCells) }) {
                        val rotation by animateFloatAsState(targetValue = if (showingCells) 180f else 0f)

                        Icon(
                            painter = painterResource(id = R.drawable.arrow_down),
                            contentDescription = null,
                            modifier = Modifier
                                .rotate(rotation)
                                .width(32.dp)
                                .height(32.dp),
                        )
                    }

                    FlowRow(
                        mainAxisSpacing = 16.dp,
                        mainAxisAlignment = FlowMainAxisAlignment.SpaceEvenly,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        FormatText(R.string.rplmn_format, properInfo?.registeredPlmn.asMccMnc)
                        FormatText(R.string.network_type_format, telephony.networkTypeName)
                        FormatText(R.string.carrier_aggregation_format, "${telephony.serviceState?.isUsingCarrierAggregation}")
                        FormatText(R.string.nr_state_format, "${NetworkRegistrationInfo.nrStateToString(telephony.serviceState?.nrState ?: -100)}/" +
                                ServiceState.frequencyRangeToString(telephony.serviceState?.nrFrequencyRange ?: -100)
                        )
                    }
                }

                AnimatedVisibility(
                    visible = expanded
                ) {
                    Column {
                        val scroll = rememberScrollState()
                        val topAlpha by animateFloatAsState(targetValue = if (scroll.value > 0) 1f else 0f)
                        val bottomAlpha by animateFloatAsState(targetValue = if (scroll.value < scroll.maxValue) 1f else 0f)

                        PaddedDivider(
                            modifier = Modifier.alpha(alpha = topAlpha)
                        )

                        Box(
                            modifier = Modifier
                                .height(300.dp)
                                .verticalScroll(scroll)
                        ) {
                            AdvancedSubInfo(telephony = telephony, subs = subs)
                        }

                        PaddedDivider(
                            modifier = Modifier.alpha(alpha = bottomAlpha)
                        )
                    }
                }

                Expander(
                    expanded = expanded,
                    onExpand = onExpand,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(20.dp)
                )
            }
        }
    }
}