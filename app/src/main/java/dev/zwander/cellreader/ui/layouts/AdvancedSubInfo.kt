package dev.zwander.cellreader.ui.layouts

import android.annotation.SuppressLint
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
import dev.zwander.cellreader.data.CellModel
import dev.zwander.cellreader.ui.components.PaddedDivider

@SuppressLint("MissingPermission")
@Composable
fun CellModel.AdvancedSubInfo(
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