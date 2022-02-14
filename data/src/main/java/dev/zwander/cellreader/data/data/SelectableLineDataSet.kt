package dev.zwander.cellreader.data.data

import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineDataSet

class SelectableLineDataSet(yVals: MutableList<Entry>, label: String?, private val line: GraphLineInfo) : LineDataSet(yVals, label) {
    var isSelected: Boolean
        get() = line.isSelected
        set(value) {
            line.isSelected = value
        }
}