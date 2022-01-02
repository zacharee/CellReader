package dev.zwander.cellreader.ui.layouts

import android.os.Build
import android.telephony.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import dev.zwander.cellreader.R
import dev.zwander.cellreader.utils.*

@Composable
fun SignalStrength(
    cellSignalStrength: CellSignalStrength,
    isFinal: Boolean,
    modifier: Modifier = Modifier
) {
    ExpanderSignalCard(
        isFinal = isFinal,
        expanded = false,
        onExpand = {},
        level = cellSignalStrength.level,
        dBm = cellSignalStrength.dbm,
        colors = listOf(
            0.0f to colorResource(id = R.color.signal_strength),
            1.0f to colorResource(id = R.color.signal_strength_1)
        ),
        modifier = modifier,
        basicInfo = {
            with(cellSignalStrength) {
                FormatText(R.string.type_format, stringResource(
                    when {
                        this is CellSignalStrengthGsm -> R.string.gsm
                        this is CellSignalStrengthWcdma -> R.string.wcdma
                        this is CellSignalStrengthCdma -> R.string.cdma
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && this is CellSignalStrengthTdscdma -> R.string.tdscdma
                        this is CellSignalStrengthLte -> R.string.lte
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && this is CellSignalStrengthNr -> R.string.nr
                        else -> R.string.unknown
                    }
                ))

                CellSignalStrength(
                    cellSignalStrength = this,
                    simple = true,
                    advanced = true
                )
            }
        }
    )
}