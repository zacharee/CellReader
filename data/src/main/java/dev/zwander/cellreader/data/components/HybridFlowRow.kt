package dev.zwander.cellreader.data.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import dev.zwander.cellreader.data.util.rememberIsWear

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HybridFlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    verticalAlignment: Alignment.Vertical = Alignment.Top,
    content: @Composable RowScope.() -> Unit
) {
    val context = LocalContext.current
    val isWear = context.rememberIsWear()

    if (!isWear) {
        Row(
            horizontalArrangement = horizontalArrangement,
            verticalAlignment = verticalAlignment,
            modifier = modifier,
            content = { content() }
        )
    } else {
        FlowRow(
            modifier = modifier,
            horizontalArrangement = horizontalArrangement,
            verticalArrangement = verticalArrangement,
            content = { content() }
        )
    }
}