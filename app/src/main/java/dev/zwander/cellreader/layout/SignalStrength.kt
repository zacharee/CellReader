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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.flowlayout.FlowRow
import com.google.accompanist.flowlayout.MainAxisAlignment
import com.google.accompanist.flowlayout.SizeMode
import dev.zwander.cellreader.R
import dev.zwander.cellreader.utils.angledGradient
import dev.zwander.cellreader.utils.asDp
import dev.zwander.cellreader.utils.cast
import dev.zwander.cellreader.utils.onAvail

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
                Text("Valid: $isValid")
                Text("ASU: $asuLevel")

                cast<CellSignalStrengthGsm>()?.apply {
                    Text("Type: GSM")
                    timingAdvance.onAvail {
                        Text("Timing Advance: $timingAdvance")
                    }
                    bitErrorRate.onAvail {
                        Text("Bit Error Rate: $bitErrorRate")
                    }
                }

                cast<CellSignalStrengthCdma>()?.apply {
                    Text("Type: CDMA")
                    Text("SnR: $evdoSnr")
                    Text("dBm: $cdmaDbm/$evdoDbm")
                    Text("Ec/Io: $cdmaEcio/$evdoEcio")
                    Text("EvDO ASU Level: $evdoAsuLevel")
                }

                cast<CellSignalStrengthWcdma>()?.apply {
                    Text("Type: WCDMA")
                    bitErrorRate.onAvail {
                        Text("Bit Error Rate: $bitErrorRate")
                    }
                    Text("RSCP: $rscp")
                    ecNo.onAvail {
                        Text("EcNo: $ecNo")
                    }
                }

                cast<CellSignalStrengthTdscdma>()?.apply {
                    Text("Type: TDSCDMA")
                    bitErrorRate.onAvail {
                        Text("Bit Error Rate: $bitErrorRate")
                    }
                    Text("RSCP: $rscp")
                }

                cast<CellSignalStrengthLte>()?.apply {
                    Text("Type: LTE")
                    timingAdvance.onAvail {
                        Text("Timing Advance: $timingAdvance")
                    }
                    Text("RSRQ: $rsrq")
                    Text("RSSI: $rssi")
                    rssnr.onAvail {
                        Text("RSSnR: $rssnr")
                    }
                    cqi.onAvail {
                        Text("CQI: $cqi")
                    }
                    cqiTableIndex.onAvail {
                        Text("CQI Index: $cqiTableIndex")
                    }
                }

                cast<CellSignalStrengthNr>()?.apply {
                    Text("Type: NR")
                    ssRsrp.onAvail {
                        Text("SS RSRP: $ssRsrp")
                    }
                    csiRsrp.onAvail {
                        Text("CSI RSRP: $csiRsrp")
                    }
                    ssRsrq.onAvail {
                        Text("SS RSRQ: $ssRsrq")
                    }
                    csiRsrq.onAvail {
                        Text("CSI RSRQ: $csiRsrq")
                    }
                    ssSinr.onAvail {
                        Text("SS SinR: $ssSinr")
                    }
                    csiSinr.onAvail {
                        Text("CSI SinR: $csiSinr")
                    }
                    if (csiCqiReport.isNotEmpty()) {
                        Text("CQI Report: ${csiCqiReport.joinToString(", ")}")
                    }
                    csiCqiTableIndex.onAvail {
                        Text("CQI Index: $it")
                    }
                }
            }
        }
    )
}