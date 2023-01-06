package dev.zwander.cellreader.data.layouts

import android.os.Build
import android.telephony.*
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import dev.zwander.cellreader.data.R
import dev.zwander.cellreader.data.components.WearSafeText
import dev.zwander.cellreader.data.data.CellSignalInfo
import dev.zwander.cellreader.data.data.CellSignalInfo.Orderer.orderOf
import dev.zwander.cellreader.data.typeString
import dev.zwander.cellreader.data.util.FormatText
import dev.zwander.cellreader.data.wrappers.*

@Composable
fun SignalStrength(
    signalStrength: SignalStrength
) {
    with (signalStrength) {
        WearSafeText(
            text = stringResource(id = R.string.signal_strength),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        FormatText(R.string.level_format, "$level")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            FormatText(R.string.timestamp_format, "$timestampMillis")
        }
    }
}

@Composable
fun CellSignalStrengthCard(
    cellSignalStrength: CellSignalStrengthWrapper,
    isFinal: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var expanded by remember {
        mutableStateOf(false)
    }

    val order = remember(cellSignalStrength) {
        cellSignalStrength.orderOf()
    }
    val hasAdvancedItems by remember(order) {
        with (order) {
            context.hasAdvancedItems
        }
    }.collectAsState(initial = false)

    ExpanderSignalCard(
        isFinal = isFinal,
        expanded = expanded,
        onExpand = { expanded = it },
        level = cellSignalStrength.level,
        dBm = cellSignalStrength.dbm,
        type = cellSignalStrength.typeString(context),
        colors = listOf(
            0.0f to colorResource(id = R.color.signal_strength),
            1.0f to colorResource(id = R.color.signal_strength_1)
        ),
        modifier = modifier,
        basicInfo = {
            CellSignalInfo.Renderer.RenderStrength(
                strength = cellSignalStrength,
                simple = true,
                advanced = false
            )
        },
        expandedInfo = if (hasAdvancedItems) {
            {
                CellSignalInfo.Renderer.RenderStrength(
                    strength = cellSignalStrength,
                    simple = false,
                    advanced = true
                )
            }
        } else {
            null
        }
    )
}