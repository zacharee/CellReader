package dev.zwander.cellreader.data.layouts.cellsignalstrength

import androidx.compose.runtime.Composable
import dev.zwander.cellreader.data.R
import dev.zwander.cellreader.data.util.FormatText
import dev.zwander.cellreader.data.util.onAvail
import dev.zwander.cellreader.data.wrappers.CellSignalStrengthCdmaWrapper

@Suppress("UNUSED_PARAMETER")
@Composable
fun CellSignalStrengthCdmaWrapper.CellSignalStrengthCdma(
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
        evdoAsuLevel.onAvail {
            FormatText(R.string.evdo_asu_format, "$it")
        }
    }
}