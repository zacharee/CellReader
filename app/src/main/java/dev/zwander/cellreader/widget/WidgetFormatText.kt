package dev.zwander.cellreader.widget

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider

@Composable
fun FormatWidgetText(
    name: String,
    value: Any?,
    modifier: GlanceModifier = GlanceModifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value.toString(),
            style = TextStyle(
                fontSize = 14.sp,
                color = ColorProvider(Color.White)
            )
        )

        Text(
            text = name,
            style = TextStyle(
                fontSize = 12.sp,
                color = ColorProvider(Color.White)
            )
        )
    }
}