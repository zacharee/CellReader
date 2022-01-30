package dev.zwander.cellreader.data.layouts

import android.os.Build
import android.telephony.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import dev.zwander.cellreader.data.endcAvailable
import dev.zwander.cellreader.data.timeStampMillisCompat
import dev.zwander.cellreader.data.layouts.cellIdentity.CellIdentity
import dev.zwander.cellreader.data.layouts.cellsignalstrength.CellSignalStrength
import dev.zwander.cellreader.data.R
import dev.zwander.cellreader.data.util.*
import dev.zwander.cellreader.data.wrappers.CellInfoLteWrapper
import dev.zwander.cellreader.data.wrappers.CellInfoWrapper
import kotlin.math.sign

@Composable
fun SignalCard(
    cellInfo: CellInfoWrapper,
    isFinal: Boolean,
    expanded: Boolean,
    onExpand: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    wear: Boolean = false
) {
    ExpanderSignalCard(
        isFinal = isFinal,
        expanded = expanded,
        onExpand = onExpand,
        level = cellInfo.cellSignalStrength.level,
        dBm = cellInfo.cellSignalStrength.dbm,
        type = stringResource(
            when (cellInfo.cellIdentity.type) {
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
                    cellSignalStrength = cellSignalStrength,
                    simple = true,
                    advanced = false
                )

                CellIdentity(
                    cellIdentity = cellIdentity,
                    simple = true,
                    advanced = false
                )
            }
        },
        expandedInfo = {
            with(cellInfo) {
                FormatText(R.string.registered_format, isRegistered.toString())
                FormatText(R.string.cell_connection_status_format, CellUtils.connectionStatusToString(connectionStatus))
                FormatText(R.string.timestamp_format, timeStamp)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    onCast<CellInfoLteWrapper> {
                        FormatText(R.string.endc_available_format, "${cellConfig.endcAvailable}")
                    }
                }

                CellSignalStrength(
                    cellSignalStrength = cellSignalStrength,
                    simple = false,
                    advanced = true
                )

                CellIdentity(
                    cellIdentity = cellIdentity,
                    simple = false,
                    advanced = true
                )
            }
        },
        wear = wear
    )
}