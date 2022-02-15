package dev.zwander.cellreader.data.data

import androidx.compose.runtime.*
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry

data class GraphLineInfo(
    val subId: Int,
    val label: String,
    val color: Int,
    val axis: YAxis.AxisDependency = YAxis.AxisDependency.LEFT,
) {
    companion object {
        const val WINDOW_SIZE = 150
    }

    var isSelected by mutableStateOf(false)

    val lineWindow = mutableStateListOf<Entry>()

    val line: MutableList<Entry> = object : ArrayList<Entry>() {
        override fun add(element: Entry): Boolean {
            lineWindow.add(element)

            if (lineWindow.size > WINDOW_SIZE) {
                lineWindow.removeAt(0)
            }

            return super.add(element)
        }

        override fun add(index: Int, element: Entry) {
            if (lineWindow.size >= WINDOW_SIZE) {
                lineWindow.removeAt(0)
            }

            lineWindow.add(index, element)

            super.add(index, element)
        }

        override fun addAll(elements: Collection<Entry>): Boolean {
            throw NotImplementedError("Use add() instead.")
        }

        override fun addAll(index: Int, elements: Collection<Entry>): Boolean {
            throw NotImplementedError("Use add() instead.")
        }

        override fun removeAll(elements: Collection<Entry>): Boolean {
            lineWindow.removeAll(elements)
            return super.removeAll(elements.toSet())
        }

        override fun remove(element: Entry): Boolean {
            lineWindow.remove(element)
            return super.remove(element)
        }
    }
}