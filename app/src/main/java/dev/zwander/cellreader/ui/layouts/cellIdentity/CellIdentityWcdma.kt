package dev.zwander.cellreader.ui.layouts.cellIdentity

import android.os.Build
import android.telephony.CellIdentityWcdma
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import dev.zwander.cellreader.R
import dev.zwander.cellreader.data.ARFCNInfo
import dev.zwander.cellreader.utils.*

@Composable
fun CellIdentityWcdma.CellIdentityWcdma(
    arfcnInfo: List<ARFCNInfo>,
    simple: Boolean,
    advanced: Boolean
) {
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

        val dlFreqs = remember(uarfcn) { arfcnInfo.map { it.dlFreq } }
        val ulFreqs = remember(uarfcn) { arfcnInfo.map { it.ulFreq } }

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