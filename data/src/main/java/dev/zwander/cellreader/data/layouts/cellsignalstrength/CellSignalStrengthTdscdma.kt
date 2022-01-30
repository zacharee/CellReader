package dev.zwander.cellreader.data.layouts.cellsignalstrength

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import dev.zwander.cellreader.data.R
import dev.zwander.cellreader.data.util.FormatText
import dev.zwander.cellreader.data.util.onAvail
import dev.zwander.cellreader.data.wrappers.CellSignalStrengthTdscdmaWrapper

@Suppress("UNUSED_PARAMETER")
@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun CellSignalStrengthTdscdmaWrapper.CellSignalStrengthTdscdma(
    simple: Boolean,
    advanced: Boolean
) {
    if (advanced) {
        rssi.onAvail {
            FormatText(R.string.rssi_format, "$rssi")
        }

        bitErrorRate.onAvail {
            FormatText(R.string.bit_error_rate_format, "$bitErrorRate")
        }

        rscp.onAvail {
            FormatText(R.string.rscp_format, "$rscp")
        }
    }
}