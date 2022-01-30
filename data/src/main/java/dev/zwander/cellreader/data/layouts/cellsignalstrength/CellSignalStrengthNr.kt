package dev.zwander.cellreader.data.layouts.cellsignalstrength

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import dev.zwander.cellreader.data.R
import dev.zwander.cellreader.data.util.FormatText
import dev.zwander.cellreader.data.util.onAvail
import dev.zwander.cellreader.data.wrappers.CellSignalStrengthNrWrapper

@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun CellSignalStrengthNrWrapper.CellSignalStrengthNr(
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
            if (!csiCqiReport.isNullOrEmpty()) {
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