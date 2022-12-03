package dev.zwander.cellreader.ui.components.bardialogs

import android.graphics.Color
import android.view.MotionEvent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.ChartTouchListener
import com.github.mikephil.charting.listener.OnChartGestureListener
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.github.mikephil.charting.utils.Utils
import dev.zwander.cellreader.data.R
import dev.zwander.cellreader.data.SubsComparator
import dev.zwander.cellreader.data.components.CardCheckbox
import dev.zwander.cellreader.data.data.CellModel
import dev.zwander.cellreader.data.data.GraphInfo
import dev.zwander.cellreader.data.data.SelectableLineDataSet
import dev.zwander.cellreader.data.util.toColorInt

@Composable
fun Graph(points: Map<Int, GraphInfo>) {
    var followData by remember {
        mutableStateOf(true)
    }

    val disabledSubIds = remember {
        mutableStateListOf<Int>()
    }

    val textColor = MaterialTheme.colorScheme.onSurface.toColorInt()

    val sortedPoints = remember(points) {
        points.toSortedMap(SubsComparator(CellModel.primaryCell.value))
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AndroidView(
            factory = {
                LineChart(it).apply {
                    xAxis.textColor = textColor
                    axisLeft.textColor = textColor
                    axisRight.textColor = textColor
                    legend.textColor = textColor
                    description.isEnabled = false
                    isHighlightPerDragEnabled = false

                    legend.isWordWrapEnabled = true
                    legend.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER

                    setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                        override fun onNothingSelected() {
                            data.dataSets.forEach { set ->
                                (set as SelectableLineDataSet).isSelected.value = false
                            }
                            notifyDataSetChanged()
                        }

                        override fun onValueSelected(e: Entry, h: Highlight) {
                            data.dataSets.forEachIndexed { index, set ->
                                (set as SelectableLineDataSet).isSelected.value = index == h.dataSetIndex
                            }
                            notifyDataSetChanged()
                        }
                    })

                    this.onChartGestureListener = object : OnChartGestureListener {
                        private var prevDist = 0f

                        override fun onChartDoubleTapped(me: MotionEvent?) {}
                        override fun onChartGestureEnd(
                            me: MotionEvent?,
                            lastPerformedGesture: ChartTouchListener.ChartGesture?
                        ) {}
                        override fun onChartGestureStart(
                            me: MotionEvent?,
                            lastPerformedGesture: ChartTouchListener.ChartGesture?
                        ) {}
                        override fun onChartLongPressed(me: MotionEvent?) {}
                        override fun onChartSingleTapped(me: MotionEvent?) {}
                        override fun onChartFling(
                            me1: MotionEvent?,
                            me2: MotionEvent?,
                            velocityX: Float,
                            velocityY: Float
                        ) {}
                        override fun onChartScale(me: MotionEvent?, scaleX: Float, scaleY: Float) {}

                        override fun onChartTranslate(me: MotionEvent?, dX: Float, dY: Float) {
                            if (dX != prevDist) {
                                prevDist = dX
                                followData = false
                            }
                        }
                    }
                }
            },
            update = {
                Utils.init(it.context)

                it.data = LineData(
                    sortedPoints.flatMap { (subId, graphInfo) ->
                        if (disabledSubIds.contains(subId)) {
                            return@flatMap listOf()
                        }

                        graphInfo.sortedLines.map { (_, line) ->
                            SelectableLineDataSet(line.lineWindow, line.label, line).apply {
                                this.mode = LineDataSet.Mode.LINEAR
                                this.setDrawCircles(false)
                                this.axisDependency = line.axis
                                this.highLightColor = Color.WHITE

                                this.fillColor = if (line.isSelected.value) Color.WHITE else line.color
                                this.circleColors = listOf(fillColor)
                                this.colors = listOf(fillColor)
                            }
                        }
                    }
                ).apply {
                    this.setDrawValues(false)
                }

                it.setVisibleXRangeMinimum(10f)
                it.setVisibleXRangeMaximum(50f)
                it.setVisibleYRangeMinimum(3f, YAxis.AxisDependency.LEFT)
                it.setVisibleYRangeMinimum(3f, YAxis.AxisDependency.RIGHT)

                it.isDoubleTapToZoomEnabled = false
                it.maxHighlightDistance = 20f
                it.xAxis.granularity = 1f
                it.axisLeft.granularity = 1f
                it.axisRight.granularity = 1f

                if (followData) {
                    it.moveViewTo(it.data.xMax, 0f, YAxis.AxisDependency.LEFT)
                } else {
                    it.invalidate()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .padding(8.dp)
        )

        Spacer(Modifier.size(8.dp))

        if (points.keys.size > 1) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(start = 16.dp, end = 16.dp)
            ) {
                sortedPoints.forEach { (subId, line) ->
                    if (line.lines.any { (_, v) -> v.line.isEmpty() }) {
                        return@forEach
                    }

                    CardCheckbox(
                        isChecked = !disabledSubIds.contains(subId),
                        onCheckedChanged = {
                            if (it) {
                                disabledSubIds.remove(subId)
                            } else if (disabledSubIds.size < points.keys.size - 1) {
                                disabledSubIds.add(subId)
                            }
                        },
                        text = stringResource(
                            id = R.string.chart_sim_format,
                            CellModel.subInfos.value[subId]?.simSlotIndex?.plus(1) ?: subId
                        ),
                        modifier = Modifier.weight(1f),
                        enabled = disabledSubIds.contains(subId) || disabledSubIds.size < points.keys.size - 1
                    )
                }
            }
        }

        CardCheckbox(
            isChecked = followData,
            onCheckedChanged = { followData = it },
            text = stringResource(id = R.string.maintain_scroll),
            modifier = Modifier.padding(16.dp)
        )
    }
}