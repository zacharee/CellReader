package dev.zwander.cellreader.ui.layouts

import android.os.Build
import android.telephony.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import dev.zwander.cellreader.R
import dev.zwander.cellreader.utils.*

@Composable
fun CellIdentity(
    cellIdentity: CellIdentity,
    simple: Boolean,
    advanced: Boolean
) {
    with (cellIdentity) {
        if (simple) {
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

            if (!operatorAlphaLong.isNullOrBlank() || !operatorAlphaShort.isNullOrBlank()) {
                FormatText(
                    R.string.operator_format,
                    setOf(
                        operatorAlphaLong,
                        operatorAlphaShort
                    ).joinToString("/")
                )
            }

            mccStringCompat?.apply {
                FormatText(R.string.mcc_mnc_format, "${mccStringCompat}-${mncStringCompat}")
            }
        }

        if (advanced) {
            channelNumber.onAvail {
                FormatText(R.string.channel_format, it.toString())
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                globalCellId?.apply {
                    FormatText(R.string.gci_format, this)
                }

                plmn?.apply {
                    FormatText(R.string.plmn_format, asMccMnc)
                }
            }
        }

        cast<CellIdentityGsm>()?.apply {
            if (advanced) {
                lac.onAvail {
                    FormatText(R.string.lac_format, "$it")
                }
                cid.onAvail {
                    FormatText(R.string.cid_format, "$it")
                }
                bsic.onAvail {
                    FormatText(R.string.bsic_format, "$bsic")
                }
                arfcn.onAvail {
                    FormatText(R.string.arfcn_format, "$arfcn")
                }
                mobileNetworkOperator?.apply {
                    if (isNotBlank()) {
                        FormatText(R.string.operator_format, this)
                    }
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    if (!additionalPlmns.isNullOrEmpty()) {
                        FormatText(
                            R.string.additional_plmns_format,
                            additionalPlmns.joinToString(", ") { it.asMccMnc }
                        )
                    }
                }
            }
        }

        cast<CellIdentityCdma>()?.apply {
            if (advanced) {
                longitude.onAvail {
                    FormatText(
                        R.string.lat_lon_format,
                        "${latitude}/${longitude}"
                    )
                }
                networkId.onAvail {
                    FormatText(R.string.cdma_network_id_format, it.toString())
                }
                basestationId.onAvail {
                    FormatText(R.string.basestation_id_format, it.toString())
                }
                systemId.onAvail {
                    FormatText(R.string.cdma_system_id_format, it.toString())
                }
            }
        }

        cast<CellIdentityWcdma>()?.apply {
            if (advanced) {
                lac.onAvail {
                    FormatText(R.string.lac_format, it.toString())
                }
                cid.onAvail {
                    FormatText(R.string.cid_format, it.toString())
                }
                uarfcn.onAvail {
                    FormatText(R.string.uarfcn_format, it.toString())
                }
                mobileNetworkOperator?.apply {
                    if (isNotBlank()) {
                        FormatText(R.string.operator_format, this)
                    }
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    if (!additionalPlmns.isNullOrEmpty()) {
                        FormatText(
                            R.string.additional_plmns_format,
                            additionalPlmns.joinToString(", ") { it.asMccMnc }
                        )
                    }

                    this.closedSubscriberGroupInfo?.apply {
                        FormatText(R.string.csg_id_format, csgIdentity.toString())
                        FormatText(
                            R.string.csg_indicator_format,
                            csgIndicator.toString()
                        )
                        FormatText(R.string.home_node_b_name_format, homeNodebName)
                    }
                }
            }
        }

        cast<CellIdentityTdscdma>()?.apply {
            if (advanced) {
                lac.onAvail {
                    FormatText(R.string.lac_format, it.toString())
                }
                cid.onAvail {
                    FormatText(R.string.cid_format, it.toString())
                }
                cpid.onAvail {
                    FormatText(R.string.cpid_format, it.toString())
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    uarfcn.onAvail {
                        FormatText(R.string.uarfcn_format, it.toString())
                    }

                    mobileNetworkOperator?.apply {
                        if (isNotBlank()) {
                            FormatText(R.string.operator_format, this)
                        }
                    }
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    if (!additionalPlmns.isNullOrEmpty()) {
                        FormatText(
                            R.string.additional_plmns_format,
                            additionalPlmns.joinToString(", ") { it.asMccMnc }
                        )
                    }

                    this.closedSubscriberGroupInfo?.apply {
                        FormatText(R.string.csg_id_format, csgIdentity.toString())
                        FormatText(
                            R.string.csg_indicator_format,
                            csgIndicator.toString()
                        )
                        FormatText(R.string.home_node_b_name_format, homeNodebName)
                    }
                }
            }
        }

        cast<CellIdentityLte>()?.apply {
            if (simple) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    if (bands.isNotEmpty()) {
                        FormatText(R.string.bands_format, bands.joinToString(", "))
                    }
                }
                bandwidth.onAvail {
                    FormatText(R.string.bandwidth_format, it.toString())
                }
            }

            if (advanced) {
                tac.onAvail {
                    FormatText(R.string.tac_format, it.toString())
                }
                ci.onAvail {
                    FormatText(R.string.ci_format, it.toString())
                }
                pci.onAvail {
                    FormatText(R.string.pci_format, it.toString())
                }
                earfcn.onAvail {
                    FormatText(R.string.earfcn_format, it.toString())
                }
                mobileNetworkOperator?.apply {
                    if (isNotBlank()) {
                        FormatText(R.string.operator_format, this)
                    }
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    if (!additionalPlmns.isNullOrEmpty()) {
                        FormatText(
                            R.string.additional_plmns_format,
                            additionalPlmns.joinToString(", ") { it.asMccMnc }
                        )
                    }

                    this.closedSubscriberGroupInfo?.apply {
                        FormatText(R.string.csg_id_format, csgIdentity.toString())
                        FormatText(
                            R.string.csg_indicator_format,
                            csgIndicator.toString()
                        )
                        FormatText(R.string.home_node_b_name_format, homeNodebName)
                    }
                }
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            cast<CellIdentityNr>()?.apply {
                if (simple) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        if (bands.isNotEmpty()) {
                            FormatText(
                                textId = R.string.bands_format,
                                bands.joinToString(", ")
                            )
                        }
                    }
                }

                if (advanced) {
                    tac.onAvail {
                        FormatText(R.string.tac_format, it.toString())
                    }
                    nci.onAvail {
                        FormatText(R.string.nci_format, it.toString())
                    }
                    pci.onAvail {
                        FormatText(
                            textId = R.string.pci_format,
                            it.toString()
                        )
                    }
                    nrarfcn.onAvail {
                        FormatText(
                            textId = R.string.nrarfcn_format,
                            it.toString()
                        )
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        if (!additionalPlmns.isNullOrEmpty()) {
                            FormatText(
                                textId = R.string.additional_plmns_format,
                                additionalPlmns.joinToString(", ") { it.asMccMnc }
                            )
                        }
                    }
                }
            }
        }
    }
}