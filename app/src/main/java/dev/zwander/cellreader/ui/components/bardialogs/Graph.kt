package dev.zwander.cellreader.ui.components.bardialogs

import android.view.MotionEvent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.ChartTouchListener
import com.github.mikephil.charting.listener.OnChartGestureListener
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.github.mikephil.charting.utils.Utils
import dev.zwander.cellreader.data.R
import dev.zwander.cellreader.data.SubsComparator
import dev.zwander.cellreader.data.components.CardCheckbox
import dev.zwander.cellreader.data.data.GraphInfo
import dev.zwander.cellreader.data.data.LocalCellModel
import dev.zwander.cellreader.data.data.SelectableLineDataSet
import dev.zwander.cellreader.data.util.instantCombine
import dev.zwander.cellreader.data.util.toColorInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.flatMapMerge

@OptIn(FlowPreview::class)
@Composable
fun Graph(points: Map<Int, GraphInfo>) {
    val cellModel = LocalCellModel.current

    var followData by remember {
        mutableStateOf(true)
    }

    val disabledSubIds = remember {
        mutableStateListOf<Int>()
    }

    val textColor = MaterialTheme.colorScheme.onSurface.toColorInt()

    val sortedPoints = remember(points) {
        points.toSortedMap(SubsComparator(cellModel.primaryCell.value))
    }

    val l = remember(sortedPoints, disabledSubIds.size) {
        instantCombine(
            sortedPoints.filterNot { disabledSubIds.contains(it.key) }.map { (_, graphInfo) ->
                instantCombine(
                    graphInfo.lines.flatMapMerge { l ->
                        instantCombine(l.map { it.value.dataSet })
                    }
                )
            }
        )
    }

    val dataSets by l.collectAsState(initial = listOf())

    val flatDataSets by remember {
        derivedStateOf {
            dataSets.flatMap { it!!.flatMap { i -> i!! } }
        }
    }

    val scope = rememberCoroutineScope()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.verticalScroll(rememberScrollState())
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

                    setVisibleXRangeMinimum(10f)
                    setVisibleXRangeMaximum(50f)
                    setVisibleYRangeMinimum(3f, YAxis.AxisDependency.LEFT)
                    setVisibleYRangeMinimum(3f, YAxis.AxisDependency.RIGHT)

                    isDoubleTapToZoomEnabled = false
                    maxHighlightDistance = 20f
                    xAxis.granularity = 1f
                    axisLeft.granularity = 1f
                    axisRight.granularity = 1f

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
                    flatDataSets
                ).apply {
                    setDrawValues(false)
                }

                if (followData) {
                    @Suppress("DeferredResultUnused")
                    scope.async(Dispatchers.Main) {
                        it.moveViewTo(it.data.xMax, 0f, YAxis.AxisDependency.LEFT)
                    }
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
                    if (line.lines.value.any { (_, v) -> v.line.isEmpty() }) {
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
                            cellModel.subInfos.value[subId]?.simSlotIndex?.plus(1) ?: subId
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