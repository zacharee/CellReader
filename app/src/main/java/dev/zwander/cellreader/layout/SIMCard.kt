package dev.zwander.cellreader.layout

import android.annotation.SuppressLint
import android.telephony.*
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import com.google.accompanist.flowlayout.FlowMainAxisAlignment
import com.google.accompanist.flowlayout.FlowRow
import dev.zwander.cellreader.R
import dev.zwander.cellreader.utils.FormatText
import dev.zwander.cellreader.utils.angledGradient
import dev.zwander.cellreader.utils.safeRegisteredPlmn

@SuppressLint("MissingPermission")
@Composable
fun SIMCard(
    telephony: TelephonyManager,
    subs: SubscriptionManager,
    subInfo: SubscriptionInfo,
    expanded: Boolean,
    onExpand: (Boolean) -> Unit,
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
                .clickable {
                    onExpand(!expanded)
                }
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val properInfo = telephony.serviceState
                ?.getNetworkRegistrationInfoListForTransportType(
                    AccessNetworkConstants.TRANSPORT_TYPE_WWAN
                )
                ?.first { it.accessNetworkTechnology != TelephonyManager.NETWORK_TYPE_IWLAN }

            FlowRow(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                mainAxisSpacing = 16.dp,
                mainAxisAlignment = FlowMainAxisAlignment.SpaceEvenly
            ) {
                subInfo.createIconBitmap(context)?.asImageBitmap()?.let {
                    Image(bitmap = it, contentDescription = null)
                }
                Text(text = "${subInfo.carrierName}")
            }

            FlowRow(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                mainAxisSpacing = 16.dp,
                mainAxisAlignment = FlowMainAxisAlignment.SpaceEvenly
            ) {
                FormatText(R.string.rplmn_format, "${StringBuilder(properInfo?.safeRegisteredPlmn ?: "000000").insert(3, "-")}")
                FormatText(R.string.network_type_format, telephony.networkTypeName)
                FormatText(R.string.carrier_aggregation_format, "${telephony.serviceState?.isUsingCarrierAggregation}")
                FormatText(R.string.nr_state_format, "${NetworkRegistrationInfo.nrStateToString(telephony.serviceState?.nrState ?: -100)}/" +
                        ServiceState.frequencyRangeToString(telephony.serviceState?.nrFrequencyRange ?: -100)
                )
            }

            AnimatedVisibility(
                visible = expanded
            ) {
                Column {
                    PaddedDivider()

                    AdvancedSubInfo(telephony = telephony, subs = subs)
                }
            }
        }
    }
}