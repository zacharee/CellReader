package dev.zwander.cellreader.data.layouts

import android.os.Build
import android.telephony.*
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import dev.zwander.cellreader.data.layouts.cellsignalstrength.CellSignalStrength
import dev.zwander.cellreader.data.R
import dev.zwander.cellreader.data.components.WearSafeText
import dev.zwander.cellreader.data.util.FormatText
import dev.zwander.cellreader.data.wrappers.*

@Composable
fun SignalStrength(
    signalStrength: SignalStrength
) {
    with (signalStrength) {
        WearSafeText(
            text = stringResource(id = R.string.signal_strength),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        FormatText(R.string.level_format, "$level")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            FormatText(R.string.timestamp_format, "$timestampMillis")
        }
    }
}

@Composable
fun CellSignalStrengthCard(
    cellSignalStrength: CellSignalStrengthWrapper,
    isFinal: Boolean,
    modifier: Modifier = Modifier,
    wear: Boolean = false
) {
    ExpanderSignalCard(
        isFinal = isFinal,
        expanded = false,
        onExpand = {},
        level = cellSignalStrength.level,
        dBm = cellSignalStrength.dbm,
        type = stringResource(
            with (cellSignalStrength) {
                when {
                    this is CellSignalStrengthGsmWrapper -> R.string.gsm
                    this is CellSignalStrengthWcdmaWrapper -> R.string.wcdma
                    this is CellSignalStrengthCdmaWrapper -> R.string.cdma
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && this is CellSignalStrengthTdscdmaWrapper -> R.string.tdscdma
                    this is CellSignalStrengthLteWrapper -> R.string.lte
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && this is CellSignalStrengthNrWrapper -> R.string.nr
                    else -> R.string.unknown
                }
            }
        ),
        colors = listOf(
            0.0f to colorResource(id = R.color.signal_strength),
            1.0f to colorResource(id = R.color.signal_strength_1)
        ),
        modifier = modifier,
        basicInfo = {
            with(cellSignalStrength) {
                CellSignalStrength(
                    cellSignalStrength = this,
                    simple = true,
                    advanced = true
                )
            }
        }
    )
}