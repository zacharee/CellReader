package dev.zwander.cellreader.ui.layouts.cellsignalstrength

import android.os.Build
import android.telephony.CellSignalStrengthTdscdma
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import dev.zwander.cellreader.R
import dev.zwander.cellreader.utils.FormatText
import dev.zwander.cellreader.utils.onAvail

@Suppress("UNUSED_PARAMETER")
@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun CellSignalStrengthTdscdma.CellSignalStrengthTdscdma(
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