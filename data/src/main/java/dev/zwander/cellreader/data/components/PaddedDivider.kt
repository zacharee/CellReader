package dev.zwander.cellreader.data.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Divider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun PaddedDivider(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
    ) {
        Spacer(modifier = Modifier.size(4.dp))
        Divider(
            color = Color.White.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.size(4.dp))
    }
}