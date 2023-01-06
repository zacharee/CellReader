package dev.zwander.cellreader.data.layouts

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import dev.zwander.cellreader.data.R
import dev.zwander.cellreader.data.data.CellSignalInfo
import dev.zwander.cellreader.data.typeString
import dev.zwander.cellreader.data.wrappers.CellInfoWrapper

@Composable
fun SignalCard(
    cellInfo: CellInfoWrapper,
    isFinal: Boolean,
    expanded: Boolean,
    onExpand: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    ExpanderSignalCard(
        isFinal = isFinal,
        expanded = expanded,
        onExpand = onExpand,
        level = cellInfo.cellSignalStrength.level,
        dBm = cellInfo.cellSignalStrength.dbm,
        type = cellInfo.cellIdentity.typeString(context),
        colors = listOf(
            0.0f to colorResource(id = R.color.cell_info),
            1.0f to colorResource(id = R.color.cell_info_1)
        ),
        modifier = modifier,
        basicInfo = {
            CellSignalInfo.Renderer.RenderIdentity(
                identity = cellInfo.cellIdentity,
                strength = cellInfo.cellSignalStrength,
                cellInfo = cellInfo,
                simple = true,
                advanced = false
            )
        }
    ) {
        CellSignalInfo.Renderer.RenderIdentity(
            identity = cellInfo.cellIdentity,
            strength = cellInfo.cellSignalStrength,
            cellInfo = cellInfo,
            simple = false,
            advanced = true
        )
    }
}