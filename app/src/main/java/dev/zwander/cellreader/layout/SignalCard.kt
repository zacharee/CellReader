package dev.zwander.cellreader.layout

import android.telephony.*
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.google.accompanist.flowlayout.FlowMainAxisAlignment
import com.google.accompanist.flowlayout.FlowRow
import com.google.accompanist.flowlayout.MainAxisAlignment
import com.google.accompanist.flowlayout.SizeMode
import dev.zwander.cellreader.R
import dev.zwander.cellreader.utils.*

@Composable
fun SignalCard(
    cellInfo: CellInfo,
    isFinal: Boolean,
    expanded: Boolean,
    onExpand: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    ExpanderSignalCard(
        isFinal = isFinal,
        expanded = expanded,
        onExpand = onExpand,
        level = cellInfo.cellSignalStrength.level,
        dBm = cellInfo.cellSignalStrength.dbm,
        colors = listOf(
            0.0f to colorResource(id = R.color.cell_info),
            1.0f to colorResource(id = R.color.cell_info_1)
        ),
        modifier = modifier,
        basicInfo = {
            with(cellInfo) {
                with(cellIdentity) {
                    with(operatorAlphaLong) {
                        if (!isNullOrBlank()) {
                            FormatText(R.string.carrier_format, this)
                        }
                    }

                    with(mccString) {
                        if (!isNullOrBlank()) {
                            FormatText(R.string.plmn_format, "${this}-${mncString}")
                        }
                    }

                    FormatText(R.string.type_format, stringResource(
                        when (type) {
                            CellInfo.TYPE_GSM -> R.string.gsm
                            CellInfo.TYPE_WCDMA -> R.string.wcdma
                            CellInfo.TYPE_CDMA -> R.string.cdma
                            CellInfo.TYPE_TDSCDMA -> R.string.tdscdma
                            CellInfo.TYPE_LTE -> R.string.lte
                            CellInfo.TYPE_NR -> R.string.nr
                            else -> R.string.unknown
                        }
                    ))

                    cast<CellIdentityLte>()?.apply {
                        FormatText(R.string.bands_format, bands.joinToString(", "))

                        bandwidth.let {
                            if (it != CellInfo.UNAVAILABLE) {
                                FormatText(R.string.bandwidth_format, "$it kHz")
                            }
                        }
                    }

                    cast<CellIdentityNr>()?.apply {
                        FormatText(R.string.bands_format, bands.joinToString(", "))
                    }
                }

                with(cellSignalStrength) {
                    cast<CellSignalStrengthLte>()?.apply {
                        FormatText(R.string.rsrq_format, "$rsrq")

                    }

                    cast<CellSignalStrengthNr>()?.apply {
                        ssRsrq.onAvail {
                            FormatText(R.string.ss_rsrq_format, it.toString())
                        }
                        csiRsrq.onAvail {
                            FormatText(R.string.csi_rsrq_format, it.toString())
                        }
                    }
                }

                cast<CellInfoLte>()?.apply {
                    FormatText(R.string.endc_available_format, "${cellConfig.endcAvailable}")
                }
            }
        },
        expandedInfo = {
            with(cellInfo) {
                with(cellSignalStrength) {
                    FormatText(R.string.asu_format, "$asuLevel")
                    FormatText(R.string.valid_format, "$isValid")

                    cast<CellSignalStrengthGsm>()?.apply {
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

                    cast<CellSignalStrengthTdscdma>()?.apply {
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

                    cast<CellSignalStrengthWcdma>()?.apply {
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

                    cast<CellSignalStrengthLte>()?.apply {
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

                    cast<CellSignalStrengthNr>()?.apply {
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

                with(cellIdentity) {
                    channelNumber.onAvail {
                        FormatText(R.string.channel_format, "$channelNumber")
                    }

                    globalCellId?.let {
                        FormatText(R.string.gci_format, globalCellId)
                    }

                    cast<CellIdentityGsm>()?.apply {
                        if (additionalPlmns.isNotEmpty()) {
                            FormatText(R.string.additional_plmns_format,
                                additionalPlmns.joinToString(", ")
                            )
                        }
                        arfcn.onAvail {
                            FormatText(R.string.arfcn_format, "$arfcn")
                        }
                        bsic.onAvail {
                            FormatText(R.string.bsic_format, "$bsic")
                        }
                        cid.onAvail {
                            FormatText(R.string.csg_id_format, "$cid")
                        }
                        lac.onAvail {
                            FormatText(R.string.lac_format, "$lac")
                        }
                        if (mobileNetworkOperator != null) {
                            FormatText(R.string.operator_format, mobileNetworkOperator)
                        }
                    }

                    cast<CellIdentityCdma>()?.apply {
                        basestationId.onAvail {
                            FormatText(R.string.basestation_id_format, "$basestationId")
                        }
                        networkId.onAvail {
                            FormatText(R.string.cdma_network_id_format, "$networkId")
                        }
                        systemId.onAvail {
                            FormatText(R.string.cdma_system_id_format, "$systemId")
                        }
                        latitude.onAvail {
                            FormatText(R.string.lat_lon_format, "$latitude/$longitude")
                        }
                    }

                    cast<CellIdentityTdscdma>()?.apply {
                        cid.onAvail {
                            FormatText(R.string.csg_id_format, "$cid")
                        }
                        cpid.onAvail {
                            FormatText(R.string.cpid_format, "$cpid")
                        }
                        lac.onAvail {
                            FormatText(R.string.lac_format, "$lac")
                        }
                        if (additionalPlmns.isNotEmpty()) {
                            FormatText(R.string.additional_plmns_format,
                                additionalPlmns.joinToString(", ")
                            )
                        }
                        closedSubscriberGroupInfo?.apply {
                            FormatText(R.string.csg_id_format, "$csgIdentity")
                            FormatText(R.string.csg_indicator_format, "$csgIndicator")
                            FormatText(R.string.home_node_b_name_format, homeNodebName)
                        }
                        if (mobileNetworkOperator != null) {
                            FormatText(R.string.operator_format, mobileNetworkOperator)
                        }
                        uarfcn.onAvail {
                            FormatText(R.string.uarfcn_format, "$uarfcn")
                        }
                    }

                    cast<CellIdentityWcdma>()?.apply {
                        cid.onAvail {
                            FormatText(R.string.cid_format, "$cid")
                        }
                        lac.onAvail {
                            FormatText(R.string.lac_format, "$lac")
                        }
                        if (additionalPlmns.isNotEmpty()) {
                            FormatText(R.string.additional_plmns_format,
                                additionalPlmns.joinToString(", ")
                            )
                        }
                        closedSubscriberGroupInfo?.apply {
                            FormatText(R.string.csg_id_format, "$csgIdentity")
                            FormatText(R.string.csg_indicator_format, "$csgIndicator")
                            FormatText(R.string.home_node_b_name_format, homeNodebName)
                        }
                        if (mobileNetworkOperator != null) {
                            FormatText(R.string.operator_format, mobileNetworkOperator)
                        }
                        psc.onAvail {
                            FormatText(R.string.psc_format, "$psc")
                        }
                        uarfcn.onAvail {
                            FormatText(R.string.uarfcn_format, "$uarfcn")
                        }
                    }

                    cast<CellIdentityLte>()?.apply {
                        ci.onAvail {
                            FormatText(R.string.ci_format, "$ci")
                        }
                        pci.onAvail {
                            FormatText(R.string.pci_format, "$pci")
                        }
                        tac.onAvail {
                            FormatText(R.string.tac_format, "$tac")
                        }
                        if (additionalPlmns.isNotEmpty()) {
                            FormatText(R.string.additional_plmns_format,
                                additionalPlmns.joinToString(", ")
                            )
                        }
                        closedSubscriberGroupInfo?.apply {
                            FormatText(R.string.csg_id_format, "$csgIdentity")
                            FormatText(R.string.csg_indicator_format, "$csgIndicator")
                            FormatText(R.string.home_node_b_name_format, homeNodebName)
                        }
                        if (mobileNetworkOperator != null) {
                            FormatText(R.string.operator_format, mobileNetworkOperator)
                        }
                        earfcn.onAvail {
                            FormatText(R.string.earfcn_format, "$earfcn")
                        }
                    }

                    cast<CellIdentityNr>()?.apply {
                        nci.onAvail {
                            FormatText(R.string.nci_format, "$nci")
                        }
                        pci.onAvail {
                            FormatText(R.string.pci_format, "$pci")
                        }
                        tac.onAvail {
                            FormatText(R.string.tac_format, "$tac")
                        }
                        if (additionalPlmns.isNotEmpty()) {
                            FormatText(R.string.additional_plmns_format,
                                additionalPlmns.joinToString(", ")
                            )
                        }
                        nrarfcn.onAvail {
                            FormatText(R.string.nrarfcn_format, "$nrarfcn")
                        }
                    }
                }
            }
        }
    )
}