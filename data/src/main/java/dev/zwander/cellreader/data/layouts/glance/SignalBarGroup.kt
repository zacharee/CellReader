package dev.zwander.cellreader.data.layouts.glance

import android.telephony.CellSignalStrength
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.layout.*
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import dev.zwander.cellreader.data.R

@Composable
fun SignalBarGroup(level: Int, dbm: Int, type: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            contentAlignment = Alignment.TopCenter
        ) {
            Text(
                text = type.first().toString(),
                style = TextStyle(
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp,
                    color = ColorProvider(Color.White)
                ),
                modifier = GlanceModifier.padding(end = 14.dp)
            )

            Image(
                provider = ImageProvider(
                    when (level) {
                        CellSignalStrength.SIGNAL_STRENGTH_POOR -> R.drawable.cell_1
                        CellSignalStrength.SIGNAL_STRENGTH_MODERATE -> R.drawable.cell_2
                        CellSignalStrength.SIGNAL_STRENGTH_GOOD -> R.drawable.cell_3
                        CellSignalStrength.SIGNAL_STRENGTH_GREAT -> R.drawable.cell_4
                        else -> R.drawable.cell_0
                    }
                ),
                contentDescription = null,
                modifier = GlanceModifier.size(32.dp),
                contentScale = ContentScale.Fit,
            )
        }

        Text(
            text = dbm.toString(),
            style = TextStyle(
                fontSize = 12.sp,
                color = ColorProvider(Color.White)
            )
        )
    }
}