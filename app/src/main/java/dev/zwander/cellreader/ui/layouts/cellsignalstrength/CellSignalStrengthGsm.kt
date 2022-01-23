package dev.zwander.cellreader.ui.layouts.cellsignalstrength

import android.os.Build
import android.telephony.CellSignalStrengthGsm
import androidx.compose.runtime.Composable
import dev.zwander.cellreader.R
import dev.zwander.cellreader.data.bitErrorRateCompat
import dev.zwander.cellreader.utils.FormatText
import dev.zwander.cellreader.utils.onAvail

@Suppress("UNUSED_PARAMETER")
@Composable
fun CellSignalStrengthGsm.CellSignalStrengthGsm(
    simple: Boolean,
    advanced: Boolean
) {
    if (advanced) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            rssi.onAvail {
                FormatText(R.string.rssi_format, "$rssi")
            }
        }
        bitErrorRateCompat.onAvail {
            FormatText(R.string.bit_error_rate_format, "$it")
        }
        timingAdvance.onAvail {
            FormatText(R.string.timing_advance_format, "$timingAdvance")
        }
    }
}