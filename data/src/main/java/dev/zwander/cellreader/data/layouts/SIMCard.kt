package dev.zwander.cellreader.data.layouts

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.os.Build
import android.telephony.AccessNetworkConstants
import android.telephony.NetworkRegistrationInfo
import android.telephony.SignalStrength
import android.telephony.TelephonyManager
import android.view.animation.AnticipateOvershootInterpolator
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Card
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp
import com.google.accompanist.flowlayout.FlowMainAxisAlignment
import com.google.accompanist.flowlayout.FlowRow
import dev.zwander.cellreader.data.R
import dev.zwander.cellreader.data.components.*
import dev.zwander.cellreader.data.util.*
import dev.zwander.cellreader.data.wrappers.ServiceStateWrapper
import dev.zwander.cellreader.data.wrappers.SubscriptionInfoWrapper
import kotlinx.coroutines.delay

@SuppressLint("MissingPermission")
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
    signalStrength: SignalStrength? = null
) {
    val context = LocalContext.current

    @Composable
    fun contents() {
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

                    subInfos[subId]?.iconBitmap?.let { bitmap ->
                        BitmapFactory.decodeByteArray(bitmap, 0, bitmap.size)?.asImageBitmap()?.let {
                            Image(
                                bitmap = it, contentDescription = null,
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

                    WearSafeText(
                        text = "${subInfos[subId]?.carrierName} $type (${rplmn})",
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
                                durationMillis = 400,
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
                                    serviceStates[subId]?.nrFrequencyRange ?: android.telephony.ServiceState.FREQUENCY_RANGE_UNKNOWN
                                )
                            )
                        }
                    }
                }

                val scroll = rememberCarouselScrollState()

                var subSize by rememberSaveable(inputs = arrayOf(subId), saver = object : Saver<MutableState<IntSize>, String> {
                    override fun restore(value: String): MutableState<IntSize> {
                        val split = value.split("x")

                        if (split.size < 2) return mutableStateOf(IntSize.Zero)

                        return mutableStateOf(IntSize(split[0].toInt(), split[1].toInt()))
                    }

                    override fun SaverScope.save(value: MutableState<IntSize>): String {
                        return "${value.value.width}x${value.value.height}"
                    }
                }) {
                    mutableStateOf(IntSize(0, context.dpAsPx(50).toInt()))
                }

                AnimatedVisibility(
                    visible = expanded,
                    enter = fadeIn() + expandVertically(
                        animationSpec = tween(
                            durationMillis = 400,
                            easing = {
                                anticipateDecelerateInterpolator(it)
                            }
                        )
                    ),
                    exit = fadeOut() + shrinkVertically(
                        animationSpec = tween(
                            durationMillis = 400,
                            easing = {
                                anticipateDecelerateInterpolator(it)
                            }
                        )
                    )
                ) {
                    Column {
                        val topAlpha by animateFloatAsState(targetValue = if (scroll.value > 0) 1f else 0f)
                        val bottomAlpha by animateFloatAsState(targetValue = if (scroll.value < scroll.maxValue) 1f else 0f)

                        PaddedDivider(
                            modifier = Modifier.alpha(alpha = topAlpha)
                        )

                        Box(
                            modifier = Modifier
                                .heightIn(max = 300.dp)
                                .animateContentSize()
                        ) {
                            var target by remember {
                                mutableStateOf(0f)
                            }
                            val scrollAlphaState by animateFloatAsState(targetValue = target)

                            LaunchedEffect(key1 = scroll.isScrollInProgress) {
                                target = if (scroll.isScrollInProgress) {
                                    1f
                                } else {
                                    delay(1000L)
                                    0f
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .height(min(subSize.height.asDp(), 300.dp))
                                    .verticalScroll(scroll)
                            ) {
                                AdvancedSubInfo(
                                    subId = subId,
                                    modifier = Modifier.onSizeChanged {
                                        subSize = it
                                    },
                                    signalStrength = signalStrength,
                                    serviceStates = serviceStates,
                                    subInfos = subInfos
                                )
                            }

                            Carousel(
                                state = scroll,
                                modifier = Modifier
                                    .width(2.dp)
                                    .height(min(subSize.height.asDp(), 300.dp))
                                    .alpha(scrollAlphaState)
                                    .align(Alignment.CenterEnd),
                                colors = CarouselDefaults.colors(thumbColor = Color.White, backgroundColor = Color.Transparent)
                            )
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

    Card(
        modifier = modifier,
        backgroundColor = Color.Transparent
    ) {
        contents()
    }
}