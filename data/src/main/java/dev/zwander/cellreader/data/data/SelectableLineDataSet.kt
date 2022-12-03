package dev.zwander.cellreader.data.data

import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineDataSet

class SelectableLineDataSet(yVals: List<Entry>, label: String?, private val line: GraphLineInfo) : LineDataSet(yVals, label) {
    val isSelected = line.isSelected
}