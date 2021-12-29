package dev.zwander.cellreader.layout

import android.telephony.*
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.flowlayout.FlowMainAxisAlignment
import com.google.accompanist.flowlayout.FlowRow
import com.google.accompanist.flowlayout.MainAxisAlignment
import com.google.accompanist.flowlayout.SizeMode
import dev.zwander.cellreader.R
import dev.zwander.cellreader.utils.cast
import dev.zwander.cellreader.utils.endcAvailable
import dev.zwander.cellreader.utils.onAvail

@Composable
fun SignalCard(cellInfo: CellInfo, expanded: Boolean, onExpand: (Boolean) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            contentAlignment = Alignment.CenterStart,
            modifier = Modifier
                .clickable {
                    onExpand(!expanded)
                }
                .padding(8.dp)
                .fillMaxWidth(),
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painter = painterResource(
                                when (cellInfo.cellSignalStrength.level) {
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

//                        AutoResizeText(
//                            text = "${cellInfo.cellSignalStrength.dbm} dBm",
//                            fontSizeRange = FontSizeRange(8.sp, 16.sp),
//                            modifier = Modifier.width(64.dp),
//                            maxLines = 1,
//                            textAlign = TextAlign.Center
//                        )

                        AutoResizingText(
                            text = "${cellInfo.cellSignalStrength.dbm} dBm",
                            modifier = Modifier.width(64.dp),
                            maxLines = 1,
                            textAlign = TextAlign.Center,
                            fontSize = 16.sp
                        )
                    }

                    Spacer(Modifier.size(16.dp))

                    FlowRow(
                        mainAxisSpacing = 16.dp,
                        mainAxisAlignment = FlowMainAxisAlignment.SpaceBetween,
                        mainAxisSize = SizeMode.Expand
                    ) {
                        with (cellInfo) {
                            with (cellIdentity) {
                                with (operatorAlphaLong) {
                                    if (!isNullOrBlank()) {
                                        Text(
                                            text = "Carrier: $this"
                                        )
                                    }
                                }

                                with (mccString) {
                                    if (!isNullOrBlank()) {
                                        Text(
                                            text = "PLMN: ${this}-${mncString}"
                                        )
                                    }
                                }

                                Text(
                                    text = "Type: ${
                                        when (type) {
                                            CellInfo.TYPE_GSM -> "GSM"
                                            CellInfo.TYPE_WCDMA -> "WCDMA"
                                            CellInfo.TYPE_CDMA -> "CDMA"
                                            CellInfo.TYPE_TDSCDMA -> "TDSCDMA"
                                            CellInfo.TYPE_LTE -> "LTE"
                                            CellInfo.TYPE_NR -> "5G NR"
                                            else -> "Unknown"
                                        }
                                    }"
                                )

                                cast<CellIdentityLte>()?.apply {
                                    Text(text = "Bands: ${bands.joinToString(", ")}")

                                    bandwidth.let {
                                        if (it != CellInfo.UNAVAILABLE) {
                                            Text(text = "Bandwidth: $it kHz")
                                        }
                                    }
                                }

                                cast<CellIdentityNr>()?.apply {
                                    Text(text = "Bands: ${bands.joinToString(", ")}")
                                }
                            }

                            with (cellSignalStrength) {
                                cast<CellSignalStrengthLte>()?.apply {
                                    Text(text = "RSRQ: $rsrq")

                                }

                                cast<CellSignalStrengthNr>()?.apply {
                                    Text(text = "RSRQ: ${csiRsrq}/${ssRsrq}")
                                }
                            }

                            cast<CellInfoLte>()?.apply {
                                Text(text = "ENDC: ${cellConfig.endcAvailable}")
                            }
                        }
                    }
                }

                AnimatedVisibility(visible = expanded) {
                    Column {
                        Spacer(Modifier.size(4.dp))

                        Divider()

                        Spacer(Modifier.size(4.dp))

                        FlowRow(
                            mainAxisSpacing = 16.dp,
                            mainAxisAlignment = MainAxisAlignment.SpaceBetween,
                            mainAxisSize = SizeMode.Expand
                        ) {
                            with (cellInfo) {
                                with (cellSignalStrength) {
                                    Text(text = "ASU: $asuLevel")
                                    Text(text = "Valid: $isValid")

                                    cast<CellSignalStrengthGsm>()?.apply {
                                        rssi.onAvail {
                                            Text(text = "RSSI: $rssi")
                                        }
                                        bitErrorRate.onAvail {
                                            Text(text = "Bit Error Rate: $bitErrorRate")
                                        }
                                        timingAdvance.onAvail {
                                            Text(text = "Timing Advance: $timingAdvance")
                                        }
                                    }

                                    cast<CellSignalStrengthCdma>()?.apply {
                                        cdmaDbm.onAvail {
                                            Text(text = "CDMA dBm: $cdmaDbm")
                                        }
                                        cdmaEcio.onAvail {
                                            Text(text = "CDMA Ec/Io: $cdmaEcio")
                                        }
                                        evdoDbm.onAvail {
                                            Text(text = "EvDO dBm: $evdoDbm")
                                        }
                                        evdoEcio.onAvail {
                                            Text(text = "EvDO Ec/Io: $evdoEcio")
                                        }
                                        evdoSnr.onAvail {
                                            Text(text = "EvDO SnR: $evdoSnr")
                                        }
                                    }

                                    cast<CellSignalStrengthTdscdma>()?.apply {
                                        rssi.onAvail {
                                            Text(text = "RSSI: $rssi")
                                        }

                                        bitErrorRate.onAvail {
                                            Text(text = "Bit Error Rate: $bitErrorRate")
                                        }

                                        rscp.onAvail {
                                            Text(text = "RSCP: $rscp")
                                        }
                                    }

                                    cast<CellSignalStrengthWcdma>()?.apply {
                                        rssi.onAvail {
                                            Text(text = "RSSI: $rssi")
                                        }

                                        bitErrorRate.onAvail {
                                            Text(text = "Bit Error Rate: $bitErrorRate")
                                        }

                                        rscp.onAvail {
                                            Text(text = "RSCP: $rscp")
                                        }

                                        ecNo.onAvail {
                                            Text(text = "Ec/No: $ecNo")
                                        }
                                    }

                                    cast<CellSignalStrengthLte>()?.apply {
                                        rssi.onAvail {
                                            Text(text = "RSSI: $rssi")
                                        }
                                        cqi.onAvail {
                                            Text(text = "CQI: $cqi")
                                        }
                                        cqiTableIndex.onAvail {
                                            Text(text = "CQI Table Index: $cqiTableIndex")
                                        }
                                        rssnr.onAvail {
                                            Text(text = "RSSnR: $rssnr")
                                        }
                                        timingAdvance.onAvail {
                                            Text(text = "Timing Advance: $timingAdvance")
                                        }
                                    }

                                    cast<CellSignalStrengthNr>()?.apply {
                                        if (csiCqiReport.isNotEmpty()) {
                                            Text(text = "CSI CQI Report: ${csiCqiReport.joinToString(", ")}")
                                        }
                                        csiCqiTableIndex.onAvail {
                                            Text(text = "CSI CQI Table Index: $csiCqiTableIndex")
                                        }
                                        ssSinr.onAvail {
                                            Text(text = "SSSinR: $ssSinr")
                                        }
                                    }
                                }

                                with (cellIdentity) {
                                    channelNumber.onAvail {
                                        Text("Channel Number: $channelNumber")
                                    }

                                    globalCellId?.let {
                                        Text(text = "GCI: $globalCellId")
                                    }

                                    cast<CellIdentityGsm>()?.apply {
                                        if (additionalPlmns.isNotEmpty()) {
                                            Text("Additional PLMNs: ${additionalPlmns.joinToString(", ")}")
                                        }
                                        arfcn.onAvail {
                                            Text("ARFCN: $arfcn")
                                        }
                                        bsic.onAvail {
                                            Text("BSIC: $bsic")
                                        }
                                        cid.onAvail {
                                            Text("CID: $cid")
                                        }
                                        lac.onAvail {
                                            Text("LAC: $lac")
                                        }
                                        if (mobileNetworkOperator != null) {
                                            Text("Operator: $mobileNetworkOperator")
                                        }
                                    }

                                    cast<CellIdentityCdma>()?.apply {
                                        basestationId.onAvail {
                                            Text("Basestation ID: $basestationId")
                                        }
                                        networkId.onAvail {
                                            Text("Network ID: $networkId")
                                        }
                                        systemId.onAvail {
                                            Text("System ID: $systemId")
                                        }
                                        latitude.onAvail {
                                            Text("Lat: $latitude")
                                        }
                                        longitude.onAvail {
                                            Text("Lon: $longitude")
                                        }
                                    }

                                    cast<CellIdentityTdscdma>()?.apply {
                                        cid.onAvail {
                                            Text("CID: $cid")
                                        }
                                        cpid.onAvail {
                                            Text("CPID: $cpid")
                                        }
                                        lac.onAvail {
                                            Text("LAC: $lac")
                                        }
                                        if (additionalPlmns.isNotEmpty()) {
                                            Text("Additional PLMNs: ${additionalPlmns.joinToString(", ")}")
                                        }
                                        closedSubscriberGroupInfo?.apply {
                                            Text("CSG Identity: $csgIdentity")
                                            Text("CSG Indicator: $csgIndicator")
                                            Text("Home Node-B Name: $homeNodebName")
                                        }
                                        if (mobileNetworkOperator != null) {
                                            Text("Operator: $mobileNetworkOperator")
                                        }
                                        uarfcn.onAvail {
                                            Text("UARFCN: $uarfcn")
                                        }
                                    }

                                    cast<CellIdentityWcdma>()?.apply {
                                        cid.onAvail {
                                            Text("CID: $cid")
                                        }
                                        lac.onAvail {
                                            Text("LAC: $lac")
                                        }
                                        if (additionalPlmns.isNotEmpty()) {
                                            Text("Additional PLMNs: ${additionalPlmns.joinToString(", ")}")
                                        }
                                        closedSubscriberGroupInfo?.apply {
                                            Text("CSG Identity: $csgIdentity")
                                            Text("CSG Indicator: $csgIndicator")
                                            Text("Home Node-B Name: $homeNodebName")
                                        }
                                        if (mobileNetworkOperator != null) {
                                            Text("Operator: $mobileNetworkOperator")
                                        }
                                        psc.onAvail {
                                            Text("PSC: $psc")
                                        }
                                        uarfcn.onAvail {
                                            Text("UARFCN: $uarfcn")
                                        }
                                    }

                                    cast<CellIdentityLte>()?.apply {
                                        ci.onAvail {
                                            Text("CI: $ci")
                                        }
                                        pci.onAvail {
                                            Text("PCI: $pci")
                                        }
                                        tac.onAvail {
                                            Text("TAC: $tac")
                                        }
                                        if (additionalPlmns.isNotEmpty()) {
                                            Text("Additional PLMNs: ${additionalPlmns.joinToString(", ")}")
                                        }
                                        closedSubscriberGroupInfo?.apply {
                                            Text("CSG Identity: $csgIdentity")
                                            Text("CSG Indicator: $csgIndicator")
                                            Text("Home Node-B Name: $homeNodebName")
                                        }
                                        if (mobileNetworkOperator != null) {
                                            Text("Operator: $mobileNetworkOperator")
                                        }
                                        earfcn.onAvail {
                                            Text("EARFCN: $earfcn")
                                        }
                                    }

                                    cast<CellIdentityNr>()?.apply {
                                        nci.onAvail {
                                            Text("NCI: $nci")
                                        }
                                        pci.onAvail {
                                            Text("PCI: $pci")
                                        }
                                        tac.onAvail {
                                            Text("TAC: $tac")
                                        }
                                        if (additionalPlmns.isNotEmpty()) {
                                            Text("Additional PLMNs: ${additionalPlmns.joinToString(", ")}")
                                        }
                                        nrarfcn.onAvail {
                                            Text("NRARFCN: $nrarfcn")
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