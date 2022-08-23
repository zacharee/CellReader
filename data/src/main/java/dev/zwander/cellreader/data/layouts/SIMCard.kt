package dev.zwander.cellreader.data.layouts

import android.annotation.SuppressLint
import android.os.Build
import android.telephony.AccessNetworkConstants
import android.telephony.NetworkRegistrationInfo
import android.telephony.ServiceState
import android.telephony.SignalStrength
import android.telephony.TelephonyManager
import android.view.animation.AnticipateOvershootInterpolator
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Card
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.flowlayout.FlowMainAxisAlignment
import com.google.accompanist.flowlayout.FlowRow
import dev.zwander.cellreader.data.LocalAnimationDuration
import dev.zwander.cellreader.data.R
import dev.zwander.cellreader.data.components.*
import dev.zwander.cellreader.data.util.*
import dev.zwander.cellreader.data.wrappers.*
import kotlin.math.absoluteValue

@SuppressLint("MissingPermission, InlinedApi")
@Composable
fun SIMCard(
    subId: Int,
    subInfos: Map<Int, SubscriptionInfoWrapper?>,
    serviceStates: Map<Int, ServiceStateWrapper?>,
    expanded: Boolean,
    onExpand: (Boolean) -> Unit,
    showingCells: Boolean,
    onShowingCells: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    strengthInfos: HashMap<Int, List<CellSignalStrengthWrapper>>,
    displayInfos: Map<Int, TelephonyDisplayInfoWrapper?>,
    signalStrengths: Map<Int, SignalStrength?>? = null,
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
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier.padding(
                    start = 8.dp,
                    top = 8.dp,
                    end = 8.dp,
                    bottom = 0.dp
                )
            ) {
                var rplmn by remember(serviceStates[subId]) {
                    mutableStateOf("000-000")
                }

                LaunchedEffect(key1 = serviceStates[subId]) {
                    rplmn = serviceStates[subId]?.getNetworkRegistrationInfoListForTransportType(
                        AccessNetworkConstants.TRANSPORT_TYPE_WWAN
                    )
                        ?.firstOrNull { it.accessNetworkTechnology != TelephonyManager.NETWORK_TYPE_IWLAN }
                        ?.rplmn.asMccMnc
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Spacer(Modifier.weight(1f))

                    subInfos[subId]?.iconBitmapBmp?.let { bitmap ->
                        bitmap.asImageBitmap().let {
                            Image(
                                bitmap = it,
                                contentDescription = null,
                                modifier = Modifier
                                    .size((16 * LocalDensity.current.fontScale).dp)
                                    .align(Alignment.CenterVertically)
                                    .shadow(
                                        elevation = 8.dp,
                                        shape = CircleShape,
                                        clip = false
                                    )
                            )

                            Spacer(Modifier.size(8.dp))
                        }
                    }

                    val type = ServiceStateWrapper.rilRadioTechnologyToString(
                        context,
                        ServiceStateWrapper.networkTypeToRilRadioTechnology(serviceStates[subId]?.dataNetworkType ?: 0)
                    )

                    val displayType = when {
                        serviceStates[subId]?.dataNetworkType == TelephonyManager.NETWORK_TYPE_IWLAN -> type
                        strengthInfos[subId]?.run {
                            any { it is CellSignalStrengthNrWrapper } &&
                                    any { it is CellSignalStrengthLteWrapper }
                        } == true -> stringResource(id = R.string.nr_nsa)
                        strengthInfos[subId]?.run {
                            any { it is CellSignalStrengthNrWrapper } &&
                                    none { it is CellSignalStrengthLteWrapper }
                        } == true -> stringResource(id = R.string.nr_sa)
                        else -> type
                    }

                    WearSafeText(
                        text = "${subInfos[subId]?.carrierName} (${rplmn}) - $displayType",
                        modifier = Modifier.align(Alignment.CenterVertically),
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.weight(1f))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    WearSafeIconButton(onClick = { onShowingCells(!showingCells) }) {
                        val rotation by animateFloatAsState(
                            targetValue = if (showingCells) 180f else 0f,
                            animationSpec = tween(
                                durationMillis = LocalAnimationDuration.current,
                                easing = {
                                    AnticipateOvershootInterpolator().getInterpolation(it)
                                }
                            )
                        )

                        WearSafeIcon(
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
                        FormatText(R.string.carrier_aggregation_format, "${serviceStates[subId]?.isUsingCarrierAggregation}")

                        serviceStates[subId]?.cellBandwidths?.filterNot { it == Int.MAX_VALUE }?.joinToString(", ")?.let { bandwidths ->
                            if (bandwidths.isNotBlank()) {
                                FormatText(R.string.bandwidths_format, bandwidths)
                            }
                        }

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            FormatText(
                                R.string.nr_state_format,
                                CellUtils.formatNrStateAndConnectionString(
                                    context,
                                    serviceStates[subId]?.nrState ?: NetworkRegistrationInfo.NR_STATE_NONE,
                                    serviceStates[subId]?.nrFrequencyRange ?: ServiceState.FREQUENCY_RANGE_UNKNOWN
                                )
                            )
                        }
                    }
                }

                val listState = rememberLazyListState()

                AnimatedVisibility(
                    visible = expanded,
                    enter = fadeIn() + expandVertically(
                        animationSpec = tween(
                            durationMillis = LocalAnimationDuration.current,
                            easing = {
                                anticipateDecelerateInterpolator(it)
                            }
                        )
                    ),
                    exit = fadeOut() + shrinkVertically(
                        animationSpec = tween(
                            durationMillis = LocalAnimationDuration.current,
                            easing = {
                                anticipateDecelerateInterpolator(it)
                            }
                        )
                    )
                ) {
                    Column {
                        val offset by remember { derivedStateOf { listState.firstVisibleItemScrollOffset } }
                        val layoutInfo by remember { derivedStateOf { listState.layoutInfo } }

                        val topAlpha by animateFloatAsState(
                            targetValue = if (offset > 0 || layoutInfo.run {
                                    (visibleItemsInfo.firstOrNull()?.index ?: 0) > 0
                                }) 1f else 0f
                        )
                        val bottomAlpha by animateFloatAsState(
                            targetValue = if (layoutInfo.run {
                                    (visibleItemsInfo.lastOrNull()?.run {
                                        index < totalItemsCount - 1 ||
                                                offset.absoluteValue < (size - viewportSize.height)
                                    } == true)
                                }) 1f else 0f
                        )

                        PaddedDivider(
                            modifier = Modifier.alpha(alpha = topAlpha)
                        )

                        Box(
                            modifier = Modifier
                                .heightIn(max = 300.dp)
                                .animateContentSize()
                        ) {
                            Box(
                                modifier = Modifier
                                    .height(300.dp)
                            ) {
                                AdvancedSubInfo(
                                    subId = subId,
                                    signalStrength = signalStrengths!![subId],
                                    serviceStates = serviceStates,
                                    subInfos = subInfos,
                                    scrollState = listState,
                                    displayInfos = displayInfos
                                )
                            }
                        }

                        PaddedDivider(
                            modifier = Modifier.alpha(alpha = bottomAlpha)
                        )
                    }
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