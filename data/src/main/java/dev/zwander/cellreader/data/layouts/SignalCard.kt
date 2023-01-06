package dev.zwander.cellreader.data.layouts

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import dev.zwander.cellreader.data.R
import dev.zwander.cellreader.data.data.CellSignalInfo
import dev.zwander.cellreader.data.data.CellSignalInfo.Orderer.orderOf
import dev.zwander.cellreader.data.typeString
import dev.zwander.cellreader.data.wrappers.CellInfoWrapper
import kotlinx.coroutines.flow.map

@Composable
fun SignalCard(
    cellInfo: CellInfoWrapper,
    isFinal: Boolean,
    expanded: Boolean,
    onExpand: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val identityOrder = remember(cellInfo.cellIdentity) {
        cellInfo.cellIdentity.orderOf()
    }
    val hasAdvancedItems by remember(identityOrder) {
        with (identityOrder) {
            context.hasAdvancedItems
        }
    }.collectAsState(initial = false)
    val hasRenderableAdvancedItems by remember(identityOrder) {
        with (identityOrder) {
            context.splitOrder.map { it.second.any { k ->
                k.canRender(cellInfo.cellIdentity) ||
                        k.canRender(cellInfo.cellSignalStrength) ||
                        k.canRender(cellInfo)
            } }
        }
    }.collectAsState(initial = hasAdvancedItems)

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
        },
        expandedInfo = if (hasAdvancedItems && hasRenderableAdvancedItems) {
            {
                CellSignalInfo.Renderer.RenderIdentity(
                    identity = cellInfo.cellIdentity,
                    strength = cellInfo.cellSignalStrength,
                    cellInfo = cellInfo,
                    simple = false,
                    advanced = true
                )
            }
        } else {
            null
        }
    )
}