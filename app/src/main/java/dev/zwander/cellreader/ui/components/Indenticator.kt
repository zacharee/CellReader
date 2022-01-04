package dev.zwander.cellreader.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import dev.zwander.cellreader.R
import dev.zwander.cellreader.utils.asDp

@Composable
fun BoxScope.IndenticatorLine(size: IntSize, isFinal: Boolean) {
    Box(
        modifier = Modifier
            .height(size.height.asDp())
            .width(16.dp)
            .padding(start = 13.503.dp, bottom = if (isFinal) (size.height / 2f + 13).asDp() else 0.dp)
            .background(Color.White, RoundedCornerShape(
                topStart = 1.25.dp,
                topEnd = 1.25.dp,
                bottomStart = if (isFinal) 0.dp else 1.25.dp,
                bottomEnd = if (isFinal) 0.dp else 1.25.dp
            ))
            .align(Alignment.TopStart)
            .animateContentSize()
    )
}

@Composable
fun IndenticatorArrow() {
    Image(
        painter = painterResource(id = R.drawable.indent_arrow),
        contentDescription = null,
        modifier = Modifier
            .width(40.dp)
            .height(32.dp)
            .padding(start = 8.dp)
    )
}