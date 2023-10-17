package dev.zwander.cellreader.data.util

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection

data class SpacedArrangement(
    override val spacing: Dp,
    val arrangement: Arrangement.HorizontalOrVertical,
) : Arrangement.HorizontalOrVertical {
    override fun Density.arrange(
        totalSize: Int,
        sizes: IntArray,
        layoutDirection: LayoutDirection,
        outPositions: IntArray
    ) {
        with (arrangement) {
            arrange(totalSize, sizes, layoutDirection, outPositions)
        }
    }

    override fun Density.arrange(totalSize: Int, sizes: IntArray, outPositions: IntArray) {
        with (arrangement) {
            arrange(totalSize, sizes, outPositions)
        }
    }
}
