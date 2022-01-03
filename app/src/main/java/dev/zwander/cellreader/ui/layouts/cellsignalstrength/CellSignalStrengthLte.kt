package dev.zwander.cellreader.ui.layouts.cellsignalstrength

import android.os.Build
import android.telephony.CellSignalStrengthLte
import androidx.compose.runtime.Composable
import dev.zwander.cellreader.R
import dev.zwander.cellreader.utils.FormatText
import dev.zwander.cellreader.utils.onAvail

@Composable
fun CellSignalStrengthLte.CellSignalStrengthLte(
    simple: Boolean,
    advanced: Boolean
) {
    if (simple) {
        FormatText(R.string.rsrq_format, "$rsrq")
    }

    if (advanced) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            rssi.onAvail {
                FormatText(R.string.rssi_format, "$rssi")
            }
        }
        cqi.onAvail {
            FormatText(R.string.cqi_format, "$cqi")
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            cqiTableIndex.onAvail {
                FormatText(R.string.cqi_table_index_format, "$cqiTableIndex")
            }
        }
        rssnr.onAvail {
            FormatText(R.string.rssnr_format, "$rssnr")
        }
        timingAdvance.onAvail {
            FormatText(R.string.timing_advance_format, "$timingAdvance")
        }
    }
}