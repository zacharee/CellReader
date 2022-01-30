package dev.zwander.cellreader.data.layouts

import android.annotation.SuppressLint
import android.telephony.SignalStrength
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.accompanist.flowlayout.FlowRow
import com.google.accompanist.flowlayout.MainAxisAlignment
import com.google.accompanist.flowlayout.SizeMode
import dev.zwander.cellreader.data.components.PaddedDivider
import dev.zwander.cellreader.data.data.CellModelBase

@SuppressLint("MissingPermission")
@Composable
fun CellModelBase.AdvancedSubInfo(
    subId: Int,
    modifier: Modifier = Modifier,
    signalStrength: SignalStrength? = null
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
                signalStrength?.apply {
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