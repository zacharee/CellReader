package dev.zwander.cellreader.ui.components

import android.telephony.CellSignalStrength
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.zwander.cellreader.R
import kotlin.math.pow
import kotlin.math.sqrt

@Composable
fun LevelIndicator(level: Int, dBm: Int, type: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box {
            Text(
                text = type.first().toString(),
                textAlign = TextAlign.Center,
                fontSize = 14.sp,
                modifier = Modifier.align(Alignment.TopCenter)
                    .padding(end = 16.dp)
            )

            Image(
                painter = painterResource(
                    when (level) {
                        CellSignalStrength.SIGNAL_STRENGTH_POOR -> R.drawable.cell_1
                        CellSignalStrength.SIGNAL_STRENGTH_MODERATE -> R.drawable.cell_2
                        CellSignalStrength.SIGNAL_STRENGTH_GOOD -> R.drawable.cell_3
                        CellSignalStrength.SIGNAL_STRENGTH_GREAT -> R.drawable.cell_4
                        else -> R.drawable.cell_0
                    }
                ),
                contentDescription = null,
                modifier = Modifier
                    .width(32.dp)
                    .height(32.dp),
                contentScale = ContentScale.Fit
            )
        }

        Text(
            text = "$dBm dBm",
            modifier = Modifier.width(64.dp),
            maxLines = 1,
            textAlign = TextAlign.Center,
            fontSize = 14.sp,
            overflow = TextOverflow.Ellipsis
        )
    }
}