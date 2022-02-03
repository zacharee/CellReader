package dev.zwander.cellreader.data.layouts.cellIdentity

import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import dev.zwander.cellreader.data.ARFCNInfo
import dev.zwander.cellreader.data.R
import dev.zwander.cellreader.data.util.FormatText
import dev.zwander.cellreader.data.util.asMccMnc
import dev.zwander.cellreader.data.util.onAvail
import dev.zwander.cellreader.data.wrappers.CellIdentityGsmWrapper

@Composable
fun CellIdentityGsmWrapper.CellIdentityGsm(
    arfcnInfo: List<ARFCNInfo>,
    simple: Boolean,
    advanced: Boolean
) {
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
        plmn?.apply {
            if (isNotBlank()) {
                FormatText(R.string.operator_format, this)
            }
        }

        val dlFreqs = remember(arfcn) { arfcnInfo.map { it.dlFreq } }
        val ulFreqs = remember(arfcn) { arfcnInfo.map { it.ulFreq } }

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
        }
    }
}