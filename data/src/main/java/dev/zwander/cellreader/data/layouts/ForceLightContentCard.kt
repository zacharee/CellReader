package dev.zwander.cellreader.data.layouts

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material.darkColors
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape

@Composable
fun ForceLightContentCard(
    modifier: Modifier = Modifier,
    shape: Shape = CardDefaults.shape,
    colors: CardColors = CardDefaults.cardColors(),
    border: BorderStroke? = null,
    elevation: CardElevation = CardDefaults.cardElevation(),
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier,
        shape = shape,
        colors =  colors,
        border = border,
        elevation = elevation
    ) {
        CompositionLocalProvider(
            LocalContentColor provides darkColors().onSurface,
            androidx.wear.compose.material.LocalContentColor provides darkColors().onSurface
        ) {
            content()
        }
    }
}
