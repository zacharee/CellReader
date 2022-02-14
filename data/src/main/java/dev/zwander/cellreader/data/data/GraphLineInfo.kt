package dev.zwander.cellreader.data.data

import androidx.compose.runtime.*
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry

data class GraphLineInfo(
    val subId: Int,
    val label: String,
    val color: Int,
    val axis: YAxis.AxisDependency = YAxis.AxisDependency.LEFT,
    val line: MutableList<Entry> = mutableStateListOf()
) {
    var isSelected by mutableStateOf(false)

    val lineWindow: MutableList<Entry> by derivedStateOf {
        line.drop(0.coerceAtLeast(line.lastIndex - 150)).toMutableList()
    }
}