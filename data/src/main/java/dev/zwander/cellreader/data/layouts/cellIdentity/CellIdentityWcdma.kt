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
import dev.zwander.cellreader.data.wrappers.CellIdentityWcdmaWrapper

@Composable
fun CellIdentityWcdmaWrapper.CellIdentityWcdma(
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
        plmn?.apply {
            if (isNotBlank()) {
                FormatText(R.string.operator_format, this.asMccMnc)
            }
        }

        val dlFreqs = rememberSaveable(inputs = arrayOf(uarfcn)) {
            arfcnInfo.map { it.dlFreq }
        }
        val ulFreqs = rememberSaveable(inputs = arrayOf(uarfcn)) {
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