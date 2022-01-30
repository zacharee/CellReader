package dev.zwander.cellreader.data.layouts.cellsignalstrength

import android.os.Build
import androidx.compose.runtime.Composable
import dev.zwander.cellreader.data.R
import dev.zwander.cellreader.data.bitErrorRateCompat
import dev.zwander.cellreader.data.util.FormatText
import dev.zwander.cellreader.data.util.onAvail
import dev.zwander.cellreader.data.wrappers.CellSignalStrengthGsmWrapper

@Suppress("UNUSED_PARAMETER")
@Composable
fun CellSignalStrengthGsmWrapper.CellSignalStrengthGsm(
    simple: Boolean,
    advanced: Boolean
) {
    if (advanced) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            rssi.onAvail {
                FormatText(R.string.rssi_format, "$rssi")
            }
        }
        bitErrorRate.onAvail {
            FormatText(R.string.bit_error_rate_format, "$it")
        }
        timingAdvance.onAvail {
            FormatText(R.string.timing_advance_format, "$timingAdvance")
        }
    }
}