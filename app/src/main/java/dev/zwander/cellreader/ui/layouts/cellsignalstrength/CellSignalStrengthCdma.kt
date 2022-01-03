package dev.zwander.cellreader.ui.layouts.cellsignalstrength

import android.os.Build
import android.telephony.CellSignalStrengthCdma
import androidx.compose.runtime.Composable
import dev.zwander.cellreader.R
import dev.zwander.cellreader.utils.FormatText
import dev.zwander.cellreader.utils.onAvail

@Suppress("UNUSED_PARAMETER")
@Composable
fun CellSignalStrengthCdma.CellSignalStrengthCdma(
    simple: Boolean,
    advanced: Boolean
) {
    if (advanced) {
        cdmaDbm.onAvail {
            FormatText(R.string.cdma_dbm_format, "$cdmaDbm")
        }
        evdoDbm.onAvail {
            FormatText(R.string.evdo_dbm_format, "$evdoDbm")
        }
        cdmaEcio.onAvail {
            FormatText(R.string.cdma_ecio_format, "$cdmaEcio")
        }
        evdoEcio.onAvail {
            FormatText(R.string.evdo_ecio_format, "$evdoEcio")
        }
        evdoSnr.onAvail {
            FormatText(R.string.snr_format, "$evdoSnr")
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            evdoAsuLevel.onAvail {
                FormatText(R.string.evdo_asu_format, "$evdoAsuLevel")
            }
        }
    }
}