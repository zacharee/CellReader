package dev.zwander.cellreader.ui.layouts.cellsignalstrength

import android.os.Build
import android.telephony.CellSignalStrengthWcdma
import androidx.compose.runtime.Composable
import dev.zwander.cellreader.R
import dev.zwander.cellreader.data.bitErrorRateCompat
import dev.zwander.cellreader.utils.FormatText
import dev.zwander.cellreader.utils.onAvail

@Suppress("UNUSED_PARAMETER")
@Composable
fun CellSignalStrengthWcdma.CellSignalStrengthWcdma(
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