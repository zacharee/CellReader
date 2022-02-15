package dev.zwander.cellreader.data.data

import android.graphics.Color
import androidx.compose.runtime.*
import androidx.lifecycle.MutableLiveData
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineDataSet

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

    var dataSet by mutableStateOf<SelectableLineDataSet?>(null)

    private val _lineWindow = MutableLiveData<MutableList<Entry>>(mutableListOf()).apply {
        observeForever {
            dataSet = SelectableLineDataSet(it, label, this@GraphLineInfo).apply {
                this.mode = LineDataSet.Mode.LINEAR
                this.setDrawCircles(false)
                this.axisDependency = axis
                this.highLightColor = Color.WHITE
            }
        }
    }

    val line: MutableList<Entry> = object : ArrayList<Entry>() {
        override fun add(element: Entry): Boolean {
            _lineWindow.value?.add(element)

            if ((_lineWindow.value?.size ?: 0) > WINDOW_SIZE) {
                _lineWindow.value?.removeAt(0)
            }

            _lineWindow.postValue(_lineWindow.value)

            return super.add(element)
        }

        override fun add(index: Int, element: Entry) {
            _lineWindow.value?.add(element)

            if ((_lineWindow.value?.size ?: 0) > WINDOW_SIZE) {
                _lineWindow.value?.removeAt(0)
            }

            _lineWindow.postValue(_lineWindow.value)

            super.add(index, element)
        }

        override fun addAll(elements: Collection<Entry>): Boolean {
            throw NotImplementedError("Use add() instead.")
        }

        override fun addAll(index: Int, elements: Collection<Entry>): Boolean {
            throw NotImplementedError("Use add() instead.")
        }

        override fun removeAll(elements: Collection<Entry>): Boolean {
            _lineWindow.value?.removeAll(elements)

            return super.removeAll(elements.toSet())
        }

        override fun remove(element: Entry): Boolean {
            _lineWindow.value?.remove(element)

            return super.remove(element)
        }
    }
}