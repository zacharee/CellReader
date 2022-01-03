package dev.zwander.cellreader.ui.layouts.cellsignalstrength

import android.os.Build
import android.telephony.*
import androidx.compose.runtime.Composable
import dev.zwander.cellreader.R
import dev.zwander.cellreader.utils.*

@Composable
fun CellSignalStrength(
    cellSignalStrength: CellSignalStrength,
    simple: Boolean,
    advanced: Boolean
) {
    with (cellSignalStrength) {
        onCast<CellSignalStrengthGsm> {
            CellSignalStrengthGsm(simple = simple, advanced = advanced)
        }

        onCast<CellSignalStrengthCdma> {
            CellSignalStrengthCdma(simple = simple, advanced = advanced)
        }

        onCast<CellSignalStrengthWcdma> {
            CellSignalStrengthWcdma(simple = simple, advanced = advanced)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            onCast<CellSignalStrengthTdscdma> {
                CellSignalStrengthTdscdma(simple = simple, advanced = advanced)
            }
        }

        onCast<CellSignalStrengthLte> {
            CellSignalStrengthLte(simple = simple, advanced = advanced)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            onCast<CellSignalStrengthNr> {
                CellSignalStrengthNr(simple = simple, advanced = advanced)
            }
        }

        if (advanced) {
            FormatText(R.string.asu_format, "$asuLevel")

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                FormatText(R.string.valid_format, "$isValid")
            }
        }
    }
}