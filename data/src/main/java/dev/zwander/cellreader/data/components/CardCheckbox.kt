package dev.zwander.cellreader.data.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Card
import androidx.compose.material.Checkbox
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun CardCheckbox(
    isChecked: Boolean,
    onCheckedChanged: (Boolean) -> Unit,
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Card(
        onClick = {
            onCheckedChanged(!isChecked)
        },
        modifier = modifier,
        enabled = enabled
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(8.dp)
        ) {
            Text(text)

            Spacer(Modifier.weight(1f))

            Checkbox(
                checked = isChecked,
                onCheckedChange = onCheckedChanged,
                enabled = enabled
            )
        }
    }
}