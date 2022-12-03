package dev.zwander.cellreader.data.data

import android.graphics.Color
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineDataSet
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

data class GraphLineInfo(
    val subId: Int,
    val label: String,
    val color: Int,
    val axis: YAxis.AxisDependency = YAxis.AxisDependency.LEFT,
) {
    companion object {
        const val WINDOW_SIZE = 150
    }

    val isSelected = MutableStateFlow(false)

    private val _lineWindow = MutableStateFlow<MutableList<Entry>>(mutableListOf())
    val lineWindow: List<Entry>
        get() = _lineWindow.value

    val line: MutableList<Entry> = object : ArrayList<Entry>() {
        override fun add(element: Entry): Boolean {
            val value = ArrayList(_lineWindow.value)

            value.add(element)

            if (value.size > WINDOW_SIZE) {
                value.removeAt(0)
            }

            _lineWindow.value = value

            return super.add(element)
        }

        override fun add(index: Int, element: Entry) {
            val value = ArrayList(_lineWindow.value)

            value.add(element)

            if (value.size > WINDOW_SIZE) {
                value.removeAt(0)
            }

            _lineWindow.value = value

            super.add(index, element)
        }

        override fun addAll(elements: Collection<Entry>): Boolean {
            throw NotImplementedError("Use add() instead.")
        }

        override fun addAll(index: Int, elements: Collection<Entry>): Boolean {
            throw NotImplementedError("Use add() instead.")
        }

        override fun removeAll(elements: Collection<Entry>): Boolean {
            val value = ArrayList(_lineWindow.value)
            val elementsSet = elements.toSet()

            value.removeAll(elementsSet)

            _lineWindow.value = value

            return super.removeAll(elementsSet)
        }

        override fun remove(element: Entry): Boolean {
            val value = ArrayList(_lineWindow.value)

            value.remove(element)

            _lineWindow.value = value

            return super.remove(element)
        }
    }
}