package dev.zwander.cellreader.ui.layouts.cellIdentity

import android.os.Build
import android.telephony.CellIdentityLte
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import dev.zwander.cellreader.R
import dev.zwander.cellreader.data.ARFCNInfo
import dev.zwander.cellreader.utils.*

@Composable
fun CellIdentityLte.CellIdentityLte(
    arfcnInfo: List<ARFCNInfo>,
    simple: Boolean,
    advanced: Boolean
) {
    if (simple) {
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

        val dlFreqs = remember(earfcn) { arfcnInfo.map { it.dlFreq } }
        val ulFreqs = remember(earfcn) { arfcnInfo.map { it.ulFreq } }

        if (dlFreqs.isNotEmpty()) {
            FormatText(
                textId = R.string.dl_freqs_format,
                dlFreqs.joinToString(", ")
            )
        }

        if (ulFreqs.isNotEmpty()) {
            FormatText(
                textId = R.string.ul_freqs_format,
                ulFreqs.joinToString(", ")
            )
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