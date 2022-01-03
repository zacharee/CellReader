package dev.zwander.cellreader.ui.layouts.cellIdentity

import android.os.Build
import android.telephony.CellIdentityTdscdma
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import dev.zwander.cellreader.R
import dev.zwander.cellreader.utils.ARFCNTools
import dev.zwander.cellreader.utils.FormatText
import dev.zwander.cellreader.utils.asMccMnc
import dev.zwander.cellreader.utils.onAvail

@Composable
fun CellIdentityTdscdma.CellIdentityTdscdma(
    simple: Boolean,
    advanced: Boolean
) {
    val arfcnInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        remember(uarfcn) {
            ARFCNTools.tdscdmaArfcnToInfo(uarfcn)
        }
    } else {
        null
    }

    if (simple) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val bands = remember(uarfcn) {
                arfcnInfo!!.map { it.band }
            }

            if (bands.isNotEmpty()) {
                FormatText(
                    textId = R.string.bands_format,
                    bands.joinToString(", ")
                )
            }
        }
    }

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

            val freqs = remember(uarfcn) { arfcnInfo!!.map { it.freq } }

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