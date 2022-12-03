package dev.zwander.cellreader.data.util

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.unit.sp
import dev.zwander.cellreader.data.components.WearSafeText

@Composable
fun FormatText(
    textId: Int,
    textFormat: Any?,
    modifier: Modifier = Modifier,
    vertical: Boolean = true
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (vertical) {
            WearSafeText(
                text = textFormat.toString(),
            )

            ProvideTextStyle(
                value = TextStyle(
                    baselineShift = BaselineShift.Superscript
                )
            ) {
                WearSafeText(
                    text = stringResource(id = textId),
                    fontSize = 12.sp,
                )
            }
        } else {
            val string = if (textFormat == null) {
                stringResource(id = textId)
            } else {
                stringResource(id = textId, textFormat)
            }

            WearSafeText(text = string)
        }
    }
}