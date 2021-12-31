package dev.zwander.cellreader.layout

import android.telephony.*
import androidx.compose.runtime.Composable
import dev.zwander.cellreader.R
import dev.zwander.cellreader.utils.FormatText
import dev.zwander.cellreader.utils.cast
import dev.zwander.cellreader.utils.onAvail

@Composable
fun CellSignalStrength(
    cellSignalStrength: CellSignalStrength,
    simple: Boolean,
    advanced: Boolean
) {
    with (cellSignalStrength) {
        cast<CellSignalStrengthGsm>()?.apply {
            if (advanced) {
                rssi.onAvail {
                    FormatText(R.string.rssi_format, "$rssi")
                }
                bitErrorRate.onAvail {
                    FormatText(R.string.bit_error_rate_format, "$bitErrorRate")
                }
                timingAdvance.onAvail {
                    FormatText(R.string.timing_advance_format, "$timingAdvance")
                }
            }
        }

        cast<CellSignalStrengthCdma>()?.apply {
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
                    FormatText(R.string.evdo_asu_format, "$evdoAsuLevel")
                }
            }
        }

        cast<CellSignalStrengthWcdma>()?.apply {
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

                ecNo.onAvail {
                    FormatText(R.string.ecno_format, "$ecNo")
                }
            }
        }

        cast<CellSignalStrengthTdscdma>()?.apply {
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

        cast<CellSignalStrengthLte>()?.apply {
            if (simple) {
                FormatText(R.string.rsrq_format, "$rsrq")
            }

            if (advanced) {
                rssi.onAvail {
                    FormatText(R.string.rssi_format, "$rssi")
                }
                cqi.onAvail {
                    FormatText(R.string.cqi_format, "$cqi")
                }
                cqiTableIndex.onAvail {
                    FormatText(R.string.cqi_table_index_format, "$cqiTableIndex")
                }
                rssnr.onAvail {
                    FormatText(R.string.rssnr_format, "$rssnr")
                }
                timingAdvance.onAvail {
                    FormatText(R.string.timing_advance_format, "$timingAdvance")
                }
            }
        }

        cast<CellSignalStrengthNr>()?.apply {
            if (simple) {
                ssRsrq.onAvail {
                    FormatText(R.string.ss_rsrq_format, it.toString())
                }
                csiRsrq.onAvail {
                    FormatText(R.string.csi_rsrq_format, it.toString())
                }
            }

            if (advanced) {
                if (csiCqiReport.isNotEmpty()) {
                    FormatText(R.string.csi_cqi_report_format, csiCqiReport.joinToString(", "))
                }
                csiCqiTableIndex.onAvail {
                    FormatText(R.string.csi_cqi_table_index_format, "$csiCqiTableIndex")
                }
                ssSinr.onAvail {
                    FormatText(R.string.ss_sinr_format, "$ssSinr")
                }
            }
        }

        if (advanced) {
            FormatText(R.string.asu_format, "$asuLevel")
            FormatText(R.string.valid_format, "$isValid")
        }
    }
}