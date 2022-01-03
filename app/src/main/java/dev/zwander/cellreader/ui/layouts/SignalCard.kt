package dev.zwander.cellreader.ui.layouts

import android.os.Build
import android.telephony.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import dev.zwander.cellreader.R
import dev.zwander.cellreader.ui.layouts.cellIdentity.CellIdentity
import dev.zwander.cellreader.ui.layouts.cellsignalstrength.CellSignalStrength
import dev.zwander.cellreader.utils.*

@Composable
fun SignalCard(
    cellInfo: CellInfo,
    isFinal: Boolean,
    expanded: Boolean,
    onExpand: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    ExpanderSignalCard(
        isFinal = isFinal,
        expanded = expanded,
        onExpand = onExpand,
        level = cellInfo.cellSignalStrengthCompat.level,
        dBm = cellInfo.cellSignalStrengthCompat.dbm,
        colors = listOf(
            0.0f to colorResource(id = R.color.cell_info),
            1.0f to colorResource(id = R.color.cell_info_1)
        ),
        modifier = modifier,
        basicInfo = {
            with(cellInfo) {
                CellIdentity(
                    cellIdentity = cellIdentityCompat,
                    simple = true,
                    advanced = false
                )

                CellSignalStrength(
                    cellSignalStrength = cellSignalStrengthCompat,
                    simple = true,
                    advanced = false
                )

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    onCast<CellInfoLte> {
                        FormatText(R.string.endc_available_format, "${cellConfig.endcAvailable}")
                    }
                }
            }
        },
        expandedInfo = {
            with(cellInfo) {
                FormatText(R.string.registered_format, isRegistered.toString())
                FormatText(R.string.cell_connection_status_format, CellUtils.connectionStatusToString(cellConnectionStatus))
                @Suppress("DEPRECATION")
                FormatText(R.string.timestamp_format, if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) timestampMillis else (timeStamp / 1000000))

                CellSignalStrength(
                    cellSignalStrength = cellSignalStrengthCompat,
                    simple = false,
                    advanced = true
                )

                CellIdentity(
                    cellIdentity = cellIdentityCompat,
                    simple = false,
                    advanced = true
                )
            }
        }
    )
}