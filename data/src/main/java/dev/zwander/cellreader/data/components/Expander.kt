package dev.zwander.cellreader.data.components

import android.view.animation.AnticipateOvershootInterpolator
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.TitleCard
import dev.zwander.cellreader.data.R

@Composable
fun Expander(
    expanded: Boolean,
    onExpand: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    @Composable
    fun contents() {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onExpand(!expanded) },
            contentAlignment = Alignment.Center
        ) {
            val rotation by animateFloatAsState(
                targetValue = if (expanded) 180f else 0f,
                animationSpec = tween(
                    durationMillis = 400,
                    easing = {
                        AnticipateOvershootInterpolator().getInterpolation(it)
                    }
                )
            )

            WearSafeIcon(
                painter = painterResource(id = R.drawable.arrow_down),
                contentDescription = null,
                modifier = Modifier.rotate(rotation)
            )
        }
    }

    Card(
        modifier = modifier,
        backgroundColor = Color.Transparent,
        elevation = 0.dp,
    ) {
        contents()
    }
}