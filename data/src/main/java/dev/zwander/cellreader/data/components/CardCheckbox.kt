package dev.zwander.cellreader.data.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardCheckbox(
    isChecked: Boolean,
    onCheckedChanged: (Boolean) -> Unit,
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    OutlinedCard(
        onClick = {
            onCheckedChanged(!isChecked)
        },
        modifier = modifier,
        enabled = enabled,
        colors = CardDefaults.outlinedCardColors(
            containerColor = Color.Transparent
        )
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