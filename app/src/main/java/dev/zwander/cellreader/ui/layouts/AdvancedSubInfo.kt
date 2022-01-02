package dev.zwander.cellreader.ui.layouts

import android.annotation.SuppressLint
import android.os.Build
import android.telephony.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.accompanist.flowlayout.FlowRow
import com.google.accompanist.flowlayout.MainAxisAlignment
import com.google.accompanist.flowlayout.SizeMode
import dev.zwander.cellreader.R
import dev.zwander.cellreader.ui.components.PaddedDivider
import dev.zwander.cellreader.serviceStates
import dev.zwander.cellreader.signalStrengths
import dev.zwander.cellreader.subInfos
import dev.zwander.cellreader.utils.*

@SuppressLint("MissingPermission")
@Composable
fun AdvancedSubInfo(
    subId: Int,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
    ) {
        ProvideTextStyle(value = LocalTextStyle.current.copy(textAlign = TextAlign.Center)) {
            FlowRow(
                mainAxisSpacing = 16.dp,
                mainAxisAlignment = MainAxisAlignment.SpaceEvenly,
                mainAxisSize = SizeMode.Expand
            ) {
                signalStrengths[subId]?.apply {
                    SignalStrength(signalStrength = this)
                }

                PaddedDivider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp)
                )

                serviceStates[subId]?.apply {
                    ServiceState(serviceState = this)
                }

                subInfos[subId]?.apply {
                    SubInfo(subscriptionInfo = this)
                }
            }
        }
    }
}