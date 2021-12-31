package dev.zwander.cellreader.layout

import android.telephony.*
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.primarySurface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.flowlayout.FlowRow
import com.google.accompanist.flowlayout.MainAxisAlignment
import com.google.accompanist.flowlayout.SizeMode
import dev.zwander.cellreader.R
import dev.zwander.cellreader.utils.*

@Composable
fun SignalStrength(
    cellSignalStrength: CellSignalStrength,
    isFinal: Boolean,
    modifier: Modifier = Modifier
) {
    ExpanderSignalCard(
        isFinal = isFinal,
        expanded = false,
        onExpand = {},
        level = cellSignalStrength.level,
        dBm = cellSignalStrength.dbm,
        colors = listOf(
            0.0f to colorResource(id = R.color.signal_strength),
            1.0f to colorResource(id = R.color.signal_strength_1)
        ),
        modifier = modifier,
        basicInfo = {
            with(cellSignalStrength) {
                cast<CellSignalStrengthGsm>()?.apply {
                    FormatText(R.string.type_format, stringResource(id = R.string.gsm))
                    timingAdvance.onAvail {
                        FormatText(R.string.timing_advance_format, "$timingAdvance")
                    }
                    bitErrorRate.onAvail {
                        FormatText(R.string.bit_error_rate_format, "$bitErrorRate")
                    }
                }

                cast<CellSignalStrengthCdma>()?.apply {
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

                cast<CellSignalStrengthWcdma>()?.apply {
                    FormatText(R.string.type_format, stringResource(id = R.string.wcdma))
                    FormatText(R.string.rscp_format, "$rscp")
                    bitErrorRate.onAvail {
                        FormatText(R.string.bit_error_rate_format, "$bitErrorRate")
                    }
                    ecNo.onAvail {
                        FormatText(R.string.ecno_format, "$ecNo")
                    }
                }

                cast<CellSignalStrengthTdscdma>()?.apply {
                    FormatText(R.string.type_format, stringResource(id = R.string.tdscdma))
                    FormatText(R.string.rscp_format, "$rscp")
                    bitErrorRate.onAvail {
                        FormatText(R.string.bit_error_rate_format, "$bitErrorRate")
                    }
                }

                cast<CellSignalStrengthLte>()?.apply {
                    FormatText(R.string.type_format, stringResource(id = R.string.lte))
                    FormatText(R.string.rsrq_format, "$rsrq")
                    FormatText(R.string.rssi_format, "$rssi")
                    timingAdvance.onAvail {
                        FormatText(R.string.timing_advance_format, "$timingAdvance")
                    }
                    rssnr.onAvail {
                        FormatText(R.string.rssnr_format, "$rssnr")
                    }
                    cqi.onAvail {
                        FormatText(R.string.cqi_format, "$cqi")
                    }
                    cqiTableIndex.onAvail {
                        FormatText(R.string.cqi_table_index_format, "$cqiTableIndex")
                    }
                }

                cast<CellSignalStrengthNr>()?.apply {
                    FormatText(R.string.type_format, stringResource(id = R.string.nr))
                    ssRsrp.onAvail {
                        FormatText(R.string.ss_rsrp_format, "$ssRsrp")
                    }
                    csiRsrp.onAvail {
                        FormatText(R.string.csi_rsrp_format, "$csiRsrp")
                    }
                    ssRsrq.onAvail {
                        FormatText(R.string.ss_rsrq_format, "$ssRsrq")
                    }
                    csiRsrq.onAvail {
                        FormatText(R.string.csi_rsrq_format, "$csiRsrq")
                    }
                    ssSinr.onAvail {
                        FormatText(R.string.ss_sinr_format, "$ssSinr")
                    }
                    csiSinr.onAvail {
                        FormatText(R.string.csi_sinr_format, "$csiSinr")
                    }
                    if (csiCqiReport.isNotEmpty()) {
                        FormatText(R.string.csi_cqi_report_format, csiCqiReport.joinToString(", "))
                    }
                    csiCqiTableIndex.onAvail {
                        FormatText(R.string.csi_cqi_table_index_format, "$it")
                    }
                }

                FormatText(R.string.valid_format, "$isValid")
                FormatText(R.string.asu_format, "$asuLevel")
            }
        }
    )
}