package dev.zwander.cellreader.data.layouts.cellIdentity

import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import dev.zwander.cellreader.data.ARFCNInfo
import dev.zwander.cellreader.data.R
import dev.zwander.cellreader.data.util.FormatText
import dev.zwander.cellreader.data.util.asMccMnc
import dev.zwander.cellreader.data.util.onAvail
import dev.zwander.cellreader.data.wrappers.CellIdentityLteWrapper

@Composable
fun CellIdentityLteWrapper.CellIdentityLte(
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
        plmn?.apply {
            if (isNotBlank()) {
                FormatText(R.string.operator_format, this)
            }
        }

        val dlFreqs = rememberSaveable(inputs = arrayOf(earfcn)) {
            arfcnInfo.map { it.dlFreq }
        }
        val ulFreqs = rememberSaveable(inputs = arrayOf(earfcn)) {
            arfcnInfo.map { it.ulFreq }
        }

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

            this.csgInfo?.apply {
                FormatText(R.string.csg_id_format, csgIdentity.toString())
                FormatText(
                    R.string.csg_indicator_format,
                    csgIndicator.toString()
                )
                FormatText(R.string.home_node_b_name_format, homeNodebName ?: "")
            }
        }
    }
}