package dev.zwander.cellreader.data.layouts.cellsignalstrength

import android.os.Build
import androidx.compose.runtime.Composable
import dev.zwander.cellreader.data.R
import dev.zwander.cellreader.data.util.FormatText
import dev.zwander.cellreader.data.util.onCast
import dev.zwander.cellreader.data.wrappers.*

@Composable
fun CellSignalStrength(
    cellSignalStrength: CellSignalStrengthWrapper,
    simple: Boolean,
    advanced: Boolean
) {
    with (cellSignalStrength) {
        onCast<CellSignalStrengthGsmWrapper> {
            CellSignalStrengthGsm(simple = simple, advanced = advanced)
        }

        onCast<CellSignalStrengthCdmaWrapper> {
            CellSignalStrengthCdma(simple = simple, advanced = advanced)
        }

        onCast<CellSignalStrengthWcdmaWrapper> {
            CellSignalStrengthWcdma(simple = simple, advanced = advanced)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            onCast<CellSignalStrengthTdscdmaWrapper> {
                CellSignalStrengthTdscdma(simple = simple, advanced = advanced)
            }
        }

        onCast<CellSignalStrengthLteWrapper> {
            CellSignalStrengthLte(simple = simple, advanced = advanced)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            onCast<CellSignalStrengthNrWrapper> {
                CellSignalStrengthNr(simple = simple, advanced = advanced)
            }
        }

        if (advanced) {
            FormatText(R.string.asu_format, "$asuLevel")

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                FormatText(R.string.valid_format, "$valid")
            }
        }
    }
}