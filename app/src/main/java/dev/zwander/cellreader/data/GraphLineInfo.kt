package dev.zwander.cellreader.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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
}