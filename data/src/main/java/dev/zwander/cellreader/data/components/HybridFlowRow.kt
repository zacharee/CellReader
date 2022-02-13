package dev.zwander.cellreader.data.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.google.accompanist.flowlayout.FlowCrossAxisAlignment
import com.google.accompanist.flowlayout.FlowMainAxisAlignment
import com.google.accompanist.flowlayout.FlowRow
import com.google.accompanist.flowlayout.SizeMode
import dev.zwander.cellreader.data.util.rememberIsWear

@Composable
fun HybridFlowRow(
    modifier: Modifier = Modifier,
    mainAxisSize: SizeMode = SizeMode.Wrap,
    mainAxisAlignment: FlowMainAxisAlignment = FlowMainAxisAlignment.Start,
    mainAxisSpacing: Dp = 0.dp,
    crossAxisAlignment: FlowCrossAxisAlignment = FlowCrossAxisAlignment.Start,
    crossAxisSpacing: Dp = 0.dp,
    lastLineMainAxisAlignment: FlowMainAxisAlignment = mainAxisAlignment,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalAlignment: Alignment.Vertical = Alignment.Top,
    content: @Composable () -> Unit
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
            modifier, mainAxisSize,
            mainAxisAlignment, mainAxisSpacing,
            crossAxisAlignment, crossAxisSpacing,
            lastLineMainAxisAlignment, content
        )
    }
}