package dev.zwander.cellreader.data.layouts.cellIdentity

import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveable
import dev.zwander.cellreader.data.ARFCNInfo
import dev.zwander.cellreader.data.R
import dev.zwander.cellreader.data.util.FormatText
import dev.zwander.cellreader.data.util.asMccMnc
import dev.zwander.cellreader.data.util.onAvail
import dev.zwander.cellreader.data.wrappers.CellIdentityTdscdmaWrapper

@Composable
fun CellIdentityTdscdmaWrapper.CellIdentityTdscdma(
    arfcnInfo: List<ARFCNInfo>,
    advanced: Boolean
) {
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

            plmn?.apply {
                if (isNotBlank()) {
                    FormatText(R.string.operator_format, this)
                }
            }

            val freqs = rememberSaveable(inputs = arrayOf(uarfcn)) {
                arfcnInfo.map { it.dlFreq }
            }

            if (freqs.isNotEmpty()) {
                FormatText(
                    textId = R.string.freqs_format,
                    freqs.joinToString(", ")
                )
            }
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