package dev.zwander.cellreader.layout

import android.telephony.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import dev.zwander.cellreader.R
import dev.zwander.cellreader.utils.*

@Composable
fun SignalCard(
    cellInfo: CellInfo,
    isFinal: Boolean,
    expanded: Boolean,
    onExpand: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    ExpanderSignalCard(
        isFinal = isFinal,
        expanded = expanded,
        onExpand = onExpand,
        level = cellInfo.cellSignalStrength.level,
        dBm = cellInfo.cellSignalStrength.dbm,
        colors = listOf(
            0.0f to colorResource(id = R.color.cell_info),
            1.0f to colorResource(id = R.color.cell_info_1)
        ),
        modifier = modifier,
        basicInfo = {
            with(cellInfo) {
                CellIdentity(
                    cellIdentity = cellIdentity,
                    simple = true,
                    advanced = false
                )

                CellSignalStrength(
                    cellSignalStrength = cellSignalStrength,
                    simple = true,
                    advanced = false
                )

                cast<CellInfoLte>()?.apply {
                    FormatText(R.string.endc_available_format, "${cellConfig.endcAvailable}")
                }
            }
        },
        expandedInfo = {
            with(cellInfo) {
                CellSignalStrength(
                    cellSignalStrength = cellSignalStrength,
                    simple = false,
                    advanced = true
                )

                CellIdentity(
                    cellIdentity = cellIdentity,
                    simple = false,
                    advanced = true
                )
            }
        }
    )
}