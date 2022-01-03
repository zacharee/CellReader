package dev.zwander.cellreader.ui.layouts

import android.annotation.SuppressLint
import android.os.Build
import android.telephony.*
import androidx.compose.runtime.Composable
import dev.zwander.cellreader.R
import dev.zwander.cellreader.utils.FormatText
import dev.zwander.cellreader.utils.bitErrorRateCompat
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
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    rssi.onAvail {
                        FormatText(R.string.rssi_format, "$rssi")
                    }
                }
                bitErrorRateCompat.onAvail {
                    FormatText(R.string.bit_error_rate_format, "$it")
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
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    evdoAsuLevel.onAvail {
                        FormatText(R.string.evdo_asu_format, "$evdoAsuLevel")
                    }
                }
            }
        }

        cast<CellSignalStrengthWcdma>()?.apply {
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
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
        }

        cast<CellSignalStrengthLte>()?.apply {
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
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
        }

        if (advanced) {
            FormatText(R.string.asu_format, "$asuLevel")

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                FormatText(R.string.valid_format, "$isValid")
            }
        }
    }
}