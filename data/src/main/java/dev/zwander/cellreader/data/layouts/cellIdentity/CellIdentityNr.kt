package dev.zwander.cellreader.data.layouts.cellIdentity

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveable
import dev.zwander.cellreader.data.ARFCNInfo
import dev.zwander.cellreader.data.R
import dev.zwander.cellreader.data.util.FormatText
import dev.zwander.cellreader.data.util.asMccMnc
import dev.zwander.cellreader.data.util.onAvail
import dev.zwander.cellreader.data.wrappers.CellIdentityNrWrapper

@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun CellIdentityNrWrapper.CellIdentityNr(
    arfcnInfo: List<ARFCNInfo>,
    advanced: Boolean
) {
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
        nrArfcn.onAvail {
            FormatText(
                textId = R.string.nrarfcn_format,
                it.toString()
            )
        }

        val dlFreqs = rememberSaveable(inputs = arrayOf(nrArfcn)) {
            arfcnInfo.map { it.dlFreq }
        }
        val ulFreqs = rememberSaveable(inputs = arrayOf(nrArfcn)) {
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
                    textId = R.string.additional_plmns_format,
                    additionalPlmns.joinToString(", ") { it.asMccMnc }
                )
            }
        }
    }
}