package dev.zwander.cellreader.data.util

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.unit.sp
import dev.zwander.cellreader.data.R
import dev.zwander.cellreader.data.components.WearSafeText

@Composable
fun FormatText(
    textId: Int,
    textFormat: Any?,
    modifier: Modifier = Modifier,
    vertical: Boolean = true,
    helpText: String? = null,
) {
    var showingHelpDialog by remember {
        mutableStateOf(false)
    }

    Column(
        modifier = modifier.then(
            if (helpText != null) {
                Modifier.clickable(
                    interactionSource = remember {
                        MutableInteractionSource()
                    },
                    indication = null,
                    onClick = {
                        showingHelpDialog = true
                    })
            } else Modifier
        ),
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

    if (showingHelpDialog) {
        AlertDialog(
            onDismissRequest = { showingHelpDialog = false },
            title = {
                Text(text = stringResource(id = R.string.info))
            },
            text = {
                Text(text = helpText ?: "")
            },
            confirmButton = {
                TextButton(onClick = { showingHelpDialog = false }) {
                    Text(text = stringResource(id = android.R.string.ok))
                }
            }
        )
    }
}