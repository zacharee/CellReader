package dev.zwander.cellreader.layout

import androidx.compose.foundation.layout.LayoutScopeMarker
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.ParentDataModifier
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import kotlin.math.ceil

@Composable
fun StaggeredVerticalGrid(
    modifier: Modifier = Modifier,
    maxColumnWidth: Dp,
    content: @Composable StaggeredGridScope.() -> Unit
) {
    Layout(
        content = { StaggeredGridScopeInstance.content() },
        modifier = modifier
    ) { measurables, constraints ->
        val placeableXY: MutableMap<Placeable, Triple<Int, Int, Int>> = mutableMapOf()

        check(constraints.hasBoundedWidth) {
            "Unbounded width not supported"
        }

        val columns = ceil(constraints.maxWidth / maxColumnWidth.toPx()).toInt()
        val columnWidth = constraints.maxWidth / columns
        val itemConstraints = constraints.copy(maxWidth = columnWidth)
        val colHeights = IntArray(columns) { 0 } // track each column's height

        val placeables = arrayListOf<Placeable>()

        for (mIndex in measurables.indices) {
            val measurable = measurables[mIndex]

            with (measurable.parentData) {
                if (this is StaggeredGridData) {
                    val column = shortestColumn(colHeights)
                    val placeable = measurable.measure(constraints)

                    placeableXY[placeable] = Triple(0, colHeights[column], column)
                    colHeights[column] += placeable.height

                    if (column == 0 && columns > 1) {
                        for (i in column + 1 until columns) {
                            colHeights[i] += placeable.height
                        }
                    }

                    if (column > 0 && columns > 1) {
                        for (i in 0 until column) {
                            colHeights[i] += placeable.height

                            val oldTriple = placeableXY[placeables[i]]

                            placeableXY[placeables[i]] = Triple(oldTriple!!.first, colHeights[i], i)
                        }

                        for (i in column + 1 until columns) {
                            colHeights[i] += placeable.height
                        }
                    }

                    placeables.add(placeable)
                } else {
                    val column = shortestColumn(colHeights)
                    val placeable = measurable.measure(itemConstraints)
                    placeableXY[placeable] = Triple(columnWidth * column, colHeights[column], column)
                    colHeights[column] += placeable.height

                    placeables.add(placeable)
                }
            }
        }

        val height = colHeights.maxOrNull()
            ?.coerceIn(constraints.minHeight, constraints.maxHeight)
            ?: constraints.minHeight
        layout(
            width = constraints.maxWidth,
            height = height
        ) {
            placeables.forEach { placeable ->
                placeable.place(
                    x = placeableXY.getValue(placeable).first,
                    y = placeableXY.getValue(placeable).second
                )
            }
        }
    }
}

private fun shortestColumn(colHeights: IntArray): Int {
    var minHeight = Int.MAX_VALUE
    var column = 0
    colHeights.forEachIndexed { index, height ->
        if (height < minHeight) {
            minHeight = height
            column = index
        }
    }
    return column
}

@LayoutScopeMarker
@Immutable
interface StaggeredGridScope {
    fun Modifier.fullSpan(): Modifier
}

internal object StaggeredGridScopeInstance : StaggeredGridScope {
    override fun Modifier.fullSpan(): Modifier {
        return this.then(
            FullSpanImpl(true)
        )
    }
}

internal class FullSpanImpl(
    val fullSpan: Boolean
) : ParentDataModifier {
    override fun Density.modifyParentData(parentData: Any?): Any {
        return ((parentData as? StaggeredGridData) ?: StaggeredGridData(fullSpan))
    }
}

internal class StaggeredGridData(
    var fullSpan: Boolean
)
