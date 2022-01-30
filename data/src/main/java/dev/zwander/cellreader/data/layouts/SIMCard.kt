package dev.zwander.cellreader.data.layouts

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.os.Build
import android.telephony.*
import android.telephony.AccessNetworkConstants
import android.util.Log
import android.view.animation.AnticipateOvershootInterpolator
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import com.google.accompanist.flowlayout.FlowMainAxisAlignment
import com.google.accompanist.flowlayout.FlowRow
import dev.zwander.cellreader.data.R
import dev.zwander.cellreader.data.components.*
import dev.zwander.cellreader.data.data.CellModelBase
import dev.zwander.cellreader.data.util.*
import dev.zwander.cellreader.data.wrappers.SubscriptionInfoWrapper
import kotlinx.coroutines.delay

@SuppressLint("MissingPermission")
@Composable
fun CellModelBase.SIMCard(
    subInfo: SubscriptionInfoWrapper?,
    expanded: Boolean,
    onExpand: (Boolean) -> Unit,
    showingCells: Boolean,
    onShowingCells: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    wear: Boolean = false,
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
                var rplmn by remember(serviceStates[subInfo?.id]) {
                    mutableStateOf("000-000")
                }

                LaunchedEffect(key1 = serviceStates[subInfo?.id]) {
                    rplmn = serviceStates[subInfo?.id]?.getNetworkRegistrationInfoListForTransportType(
                        AccessNetworkConstants.TRANSPORT_TYPE_WWAN
                    )
                        ?.first { it.accessNetworkTechnology != TelephonyManager.NETWORK_TYPE_IWLAN }
                        ?.rplmn.asMccMnc
                }

                FlowRow(
                    mainAxisSpacing = 16.dp,
                    mainAxisAlignment = FlowMainAxisAlignment.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    subInfo?.iconBitmap?.let { bitmap ->
                        BitmapFactory.decodeByteArray(bitmap, 0, bitmap.size)?.asImageBitmap()?.let {
                            Image(bitmap = it, contentDescription = null)
                        }
                    }
                    Text(text = "${subInfo?.carrierName}")
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { onShowingCells(!showingCells) }) {
                        val rotation by animateFloatAsState(
                            targetValue = if (showingCells) 180f else 0f,
                            animationSpec = tween(
                                durationMillis = 400,
                                easing = {
                                    AnticipateOvershootInterpolator().getInterpolation(it)
                                }
                            )
                        )

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
                        FormatText(R.string.rplmn_format, rplmn)
                        FormatText(R.string.network_type_format, context.resources.getString(
                            when (serviceStates[subInfo?.id]?.dataNetworkType) {
                                CellInfo.TYPE_GSM -> R.string.gsm
                                CellInfo.TYPE_WCDMA -> R.string.wcdma
                                CellInfo.TYPE_CDMA -> R.string.cdma
                                CellInfo.TYPE_TDSCDMA -> R.string.tdscdma
                                CellInfo.TYPE_LTE -> R.string.lte
                                CellInfo.TYPE_NR -> R.string.nr
                                else -> R.string.unknown
                            }
                        ))
                        FormatText(R.string.carrier_aggregation_format, "${serviceStates[subInfo?.id]?.isUsingCarrierAggregation}")

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            FormatText(R.string.nr_state_format, "${
                                CellUtils.nrStateToString(
                                    serviceStates[subInfo?.id]?.nrState ?: -100)}/" +
                                    CellUtils.frequencyRangeToString(serviceStates[subInfo?.id]?.nrFrequencyRange ?: -100)
                            )
                        }
                    }
                }

                val scroll = rememberCarouselScrollState()

                var subSize by remember {
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
                                    subId = subInfo?.id ?: 0,
                                    modifier = Modifier.onSizeChanged {
                                        subSize = it
                                    },
                                    signalStrength = signalStrength
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

    if (!wear) {
        Card(
            modifier = modifier,
            backgroundColor = Color.Transparent
        ) {
            contents()
        }
    } else {
        androidx.wear.compose.material.Card(
            onClick = {},
            modifier = modifier,
            backgroundPainter = ColorPainter(Color.Transparent)
        ) {
            contents()
        }
    }
}