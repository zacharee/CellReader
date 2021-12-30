package dev.zwander.cellreader.layout

import android.telephony.*
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
    var size by remember {
        mutableStateOf(IntSize.Zero)
    }

    Box(
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .height(size.height.asDp())
                .width(16.dp)
                .padding(start = 13.5.dp, bottom = if (isFinal) (size.height / 2f).asDp() else 0.dp)
                .background(Color.White, RoundedCornerShape(1.25.dp))
                .align(Alignment.CenterStart)
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.onSizeChanged {
                size = it
            }
        ) {
            Image(
                painter = painterResource(id = R.drawable.indent_arrow),
                contentDescription = null,
                modifier = Modifier.width(40.dp).height(32.dp).padding(start = 8.dp)
            )

            Card(
                backgroundColor = Color.Transparent,
                elevation = 8.dp
            ) {
                Box(
                    contentAlignment = Alignment.CenterStart,
                    modifier = Modifier.angledGradient(
                        listOf(
                            0f to colorResource(id = R.color.signal_strength_1),
                            1f to colorResource(id = R.color.signal_strength),
                        ),
                        87f
                    ).padding(8.dp),
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Image(
                                    painter = painterResource(
                                        when (cellSignalStrength.level) {
                                            CellSignalStrength.SIGNAL_STRENGTH_POOR -> R.drawable.cell_1
                                            CellSignalStrength.SIGNAL_STRENGTH_MODERATE -> R.drawable.cell_2
                                            CellSignalStrength.SIGNAL_STRENGTH_GOOD -> R.drawable.cell_3
                                            CellSignalStrength.SIGNAL_STRENGTH_GREAT -> R.drawable.cell_4
                                            else -> R.drawable.cell_0
                                        }
                                    ),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .width(32.dp)
                                        .height(32.dp),
                                    contentScale = ContentScale.Fit
                                )

                                Spacer(Modifier.size(8.dp))

                                AutoResizingText(
                                    text = "${cellSignalStrength.dbm} dBm",
                                    modifier = Modifier.width(64.dp),
                                    maxLines = 1,
                                    textAlign = TextAlign.Center,
                                    fontSize = 16.sp
                                )
                            }

                            Spacer(Modifier.size(16.dp))

                            FlowRow(
                                mainAxisSpacing = 16.dp,
                                mainAxisAlignment = MainAxisAlignment.SpaceBetween,
                                mainAxisSize = SizeMode.Expand,
                            ) {
                                with(cellSignalStrength) {
                                    Text("Valid: $isValid")
                                    Text("ASU: $asuLevel")

                                    cast<CellSignalStrengthGsm>()?.apply {
                                        Text("Type: GSM")
                                        timingAdvance.onAvail {
                                            Text("Timing Advance: $timingAdvance")
                                        }
                                        bitErrorRate.onAvail {
                                            Text("Bit Error Rate: $bitErrorRate")}
                                    }

                                    cast<CellSignalStrengthCdma>()?.apply {
                                        Text("Type: CDMA")
                                        Text("SnR: $evdoSnr")
                                        Text("dBm: $cdmaDbm/$evdoDbm")
                                        Text("Ec/Io: $cdmaEcio/$evdoEcio")
                                        Text("EvDO ASU Level: $evdoAsuLevel")}

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
                                        Text("RSCP: $rscp")}

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
                        }
                    }
                }
            }
        }
    }
}