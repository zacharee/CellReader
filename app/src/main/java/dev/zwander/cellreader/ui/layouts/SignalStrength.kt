package dev.zwander.cellreader.ui.layouts

import android.os.Build
import android.telephony.*
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import dev.zwander.cellreader.R
import dev.zwander.cellreader.signalStrengths
import dev.zwander.cellreader.utils.*

@Composable
fun SignalStrength(
    signalStrength: SignalStrength
) {
    with (signalStrength) {
        Text(
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
fun CellSignalStrength(
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