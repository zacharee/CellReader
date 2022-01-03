package dev.zwander.cellreader.ui.layouts.cellsignalstrength

import android.os.Build
import android.telephony.CellSignalStrengthNr
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import dev.zwander.cellreader.R
import dev.zwander.cellreader.utils.FormatText
import dev.zwander.cellreader.utils.onAvail

@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun CellSignalStrengthNr.CellSignalStrengthNr(
    simple: Boolean,
    advanced: Boolean
) {
    if (simple) {
        ssRsrq.onAvail {
            FormatText(R.string.ss_rsrq_format, it.toString())
        }
        csiRsrq.onAvail {
            FormatText(R.string.csi_rsrq_format, it.toString())
        }
    }

    if (advanced) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (csiCqiReport.isNotEmpty()) {
                FormatText(R.string.csi_cqi_report_format, csiCqiReport.joinToString(", "))
            }

            csiCqiTableIndex.onAvail {
                FormatText(R.string.csi_cqi_table_index_format, "$csiCqiTableIndex")
            }
        }
        ssSinr.onAvail {
            FormatText(R.string.ss_sinr_format, "$ssSinr")
        }
    }
}