package dev.zwander.cellreader.data.layouts.cellIdentity

import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import dev.zwander.cellreader.data.ARFCNTools
import dev.zwander.cellreader.data.R
import dev.zwander.cellreader.data.getBands
import dev.zwander.cellreader.data.util.FormatText
import dev.zwander.cellreader.data.util.onAvail
import dev.zwander.cellreader.data.util.onCast
import dev.zwander.cellreader.data.wrappers.*

@Composable
fun CellIdentity(
    cellIdentity: CellIdentityWrapper,
    simple: Boolean,
    advanced: Boolean
) {
    with (cellIdentity) {
        val arfcnInfo = remember(channelNumber) {
            ARFCNTools.getInfo(channelNumber, type)
        }
        val bands = remember(channelNumber) {
            getBands(arfcnInfo)
        }

        if (simple) {
            if (bands.isNotEmpty()) {
                FormatText(
                    R.string.bands_format,
                    bands.joinToString(", ")
                )
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
            }
        }

        onCast<CellIdentityGsmWrapper> {
            CellIdentityGsm(arfcnInfo = arfcnInfo, simple = simple, advanced = advanced)
        }

        onCast<CellIdentityCdmaWrapper> {
            CellIdentityCdma(simple = simple, advanced = advanced)
        }

        onCast<CellIdentityWcdmaWrapper> {
            CellIdentityWcdma(arfcnInfo = arfcnInfo, simple = simple, advanced = advanced)
        }

        onCast<CellIdentityTdscdmaWrapper> {
            CellIdentityTdscdma(arfcnInfo = arfcnInfo, simple = simple, advanced = advanced)
        }

        onCast<CellIdentityLteWrapper> {
            CellIdentityLte(arfcnInfo = arfcnInfo, simple = simple, advanced = advanced)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            onCast<CellIdentityNrWrapper> {
                CellIdentityNr(arfcnInfo = arfcnInfo, simple = simple, advanced = advanced)
            }
        }

        if (simple) {
            if (!alphaLong.isNullOrBlank() || !alphaShort.isNullOrBlank()) {
                FormatText(
                    R.string.operator_format,
                    setOf(
                        alphaLong,
                        alphaShort
                    ).joinToString("/")
                )
            }

            mcc?.apply {
                FormatText(R.string.plmn_format, "${mcc}-${mnc}")
            }
        }
    }
}