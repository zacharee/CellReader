package dev.zwander.cellreader.ui.layouts

import android.os.Build
import android.telephony.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import dev.zwander.cellreader.R
import dev.zwander.cellreader.data.endcAvailable
import dev.zwander.cellreader.data.timeStampMillisCompat
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
        type = stringResource(
            when (cellInfo.cellIdentityCompat.type) {
                CellInfo.TYPE_GSM -> R.string.gsm
                CellInfo.TYPE_WCDMA -> R.string.wcdma
                CellInfo.TYPE_CDMA -> R.string.cdma
                CellInfo.TYPE_TDSCDMA -> R.string.tdscdma
                CellInfo.TYPE_LTE -> R.string.lte
                CellInfo.TYPE_NR -> R.string.nr
                else -> R.string.unknown
            }
        ),
        colors = listOf(
            0.0f to colorResource(id = R.color.cell_info),
            1.0f to colorResource(id = R.color.cell_info_1)
        ),
        modifier = modifier,
        basicInfo = {
            with(cellInfo) {
                CellSignalStrength(
                    cellSignalStrength = cellSignalStrengthCompat,
                    simple = true,
                    advanced = false
                )

                CellIdentity(
                    cellIdentity = cellIdentityCompat,
                    simple = true,
                    advanced = false
                )
            }
        },
        expandedInfo = {
            with(cellInfo) {
                FormatText(R.string.registered_format, isRegistered.toString())
                FormatText(R.string.cell_connection_status_format, CellUtils.connectionStatusToString(cellConnectionStatus))
                FormatText(R.string.timestamp_format, timeStampMillisCompat)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    onCast<CellInfoLte> {
                        FormatText(R.string.endc_available_format, "${cellConfig.endcAvailable}")
                    }
                }

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