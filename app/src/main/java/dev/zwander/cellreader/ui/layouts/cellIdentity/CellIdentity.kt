package dev.zwander.cellreader.ui.layouts.cellIdentity

import android.os.Build
import android.telephony.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import dev.zwander.cellreader.R
import dev.zwander.cellreader.utils.*

@Composable
fun CellIdentity(
    cellIdentity: CellIdentity,
    simple: Boolean,
    advanced: Boolean
) {
    with (cellIdentity) {
        if (simple) {
            FormatText(R.string.type_format, stringResource(
                when (type) {
                    CellInfo.TYPE_GSM -> R.string.gsm
                    CellInfo.TYPE_WCDMA -> R.string.wcdma
                    CellInfo.TYPE_CDMA -> R.string.cdma
                    CellInfo.TYPE_TDSCDMA -> R.string.tdscdma
                    CellInfo.TYPE_LTE -> R.string.lte
                    CellInfo.TYPE_NR -> R.string.nr
                    else -> R.string.unknown
                }
            ))

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
            CellIdentityGsm(simple = simple, advanced = advanced)
        }

        onCast<CellIdentityCdma> {
            CellIdentityCdma(simple = simple, advanced = advanced)
        }

        onCast<CellIdentityWcdma> {
            CellIdentityWcdma(simple = simple, advanced = advanced)
        }

        onCast<CellIdentityTdscdma> {
            CellIdentityTdscdma(simple = simple, advanced = advanced)
        }

        onCast<CellIdentityLte> {
            CellIdentityLte(simple = simple, advanced = advanced)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            onCast<CellIdentityNr> {
                CellIdentityNr(simple = simple, advanced = advanced)
            }
        }
    }
}