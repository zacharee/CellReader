package dev.zwander.cellreader.layout

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import dev.zwander.cellreader.R

@Composable
fun Expander(
    expanded: Boolean,
    onExpand: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        backgroundColor = Color.Transparent,
        elevation = 0.dp,
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
                .clickable { onExpand(!expanded) },
            contentAlignment = Alignment.Center
        ) {
            val rotation by animateFloatAsState(targetValue = if (expanded) 180f else 0f)

            Icon(
                painter = painterResource(id = R.drawable.arrow_down),
                contentDescription = null,
                modifier = Modifier.rotate(rotation)
            )
        }
    }
}