package dev.zwander.cellreader.data.layouts.cellsignalstrength

import android.os.Build
import androidx.compose.runtime.Composable
import dev.zwander.cellreader.data.R
import dev.zwander.cellreader.data.util.FormatText
import dev.zwander.cellreader.data.util.onAvail
import dev.zwander.cellreader.data.wrappers.CellSignalStrengthWcdmaWrapper

@Suppress("UNUSED_PARAMETER")
@Composable
fun CellSignalStrengthWcdmaWrapper.CellSignalStrengthWcdma(
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
            FormatText(R.string.bit_error_rate_format, "$bitErrorRate")
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            rscp.onAvail {
                FormatText(R.string.rscp_format, "$rscp")
            }

            ecNo.onAvail {
                FormatText(R.string.ecno_format, "$ecNo")
            }
        }
    }
}