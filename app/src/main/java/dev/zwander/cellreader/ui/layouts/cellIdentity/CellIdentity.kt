package dev.zwander.cellreader.ui.layouts.cellIdentity

import android.os.Build
import android.telephony.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import dev.zwander.cellreader.R
import dev.zwander.cellreader.data.ARFCNTools
import dev.zwander.cellreader.data.getBands
import dev.zwander.cellreader.utils.*

@Composable
fun CellIdentity(
    cellIdentity: CellIdentity,
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

                plmn?.apply {
                    FormatText(R.string.plmn_format, asMccMnc)
                }
            }
        }

        onCast<CellIdentityGsm> {
            CellIdentityGsm(arfcnInfo = arfcnInfo, simple = simple, advanced = advanced)
        }

        onCast<CellIdentityCdma> {
            CellIdentityCdma(simple = simple, advanced = advanced)
        }

        onCast<CellIdentityWcdma> {
            CellIdentityWcdma(arfcnInfo = arfcnInfo, simple = simple, advanced = advanced)
        }

        onCast<CellIdentityTdscdma> {
            CellIdentityTdscdma(arfcnInfo = arfcnInfo, simple = simple, advanced = advanced)
        }

        onCast<CellIdentityLte> {
            CellIdentityLte(arfcnInfo = arfcnInfo, simple = simple, advanced = advanced)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            onCast<CellIdentityNr> {
                CellIdentityNr(arfcnInfo = arfcnInfo, simple = simple, advanced = advanced)
            }
        }

        if (simple) {
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
    }
}