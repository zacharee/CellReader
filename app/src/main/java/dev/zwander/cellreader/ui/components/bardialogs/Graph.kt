@file:OptIn(ExperimentalMaterialApi::class)

package dev.zwander.cellreader.ui.components.bardialogs

import android.view.MotionEvent
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.listener.ChartTouchListener
import com.github.mikephil.charting.listener.OnChartGestureListener
import com.github.mikephil.charting.utils.Utils
import dev.zwander.cellreader.data.GraphInfo
import dev.zwander.cellreader.data.R
import dev.zwander.cellreader.data.util.toColorInt

@Composable
fun Graph(points: Map<Int, GraphInfo>) {
    var followData by remember {
        mutableStateOf(true)
    }

    val textColor = MaterialTheme.colors.onSurface.toColorInt()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AndroidView(
            factory = {
                Utils.init(it)
                LineChart(it).apply {
                    xAxis.textColor = textColor
                    axisLeft.textColor = textColor
                    axisRight.textColor = textColor
                    legend.textColor = textColor
                    axisLeft.granularity = 1.0f
                    description.isEnabled = false
                    isHighlightPerTapEnabled = false
                    isHighlightPerDragEnabled = false

                    legend.isWordWrapEnabled = true
                    legend.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER

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
                        override fun onChartScale(me: MotionEvent?, scaleX: Float, scaleY: Float) {}
                        override fun onChartSingleTapped(me: MotionEvent?) {}
                        override fun onChartFling(
                            me1: MotionEvent?,
                            me2: MotionEvent?,
                            velocityX: Float,
                            velocityY: Float
                        ) {}
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
                it.data = LineData(
                    points.flatMap { (_, graphInfo) ->
                        graphInfo.lines.toSortedMap().map { (_, line) ->
                            LineDataSet(line.line, line.label).apply {
                                this.mode = LineDataSet.Mode.LINEAR
                                this.fillColor = line.color
                                this.circleColors = listOf(fillColor)
                                this.colors = listOf(fillColor)
                                this.setDrawCircles(false)
                                this.axisDependency = line.axis
                            }
                        }
                    }
                ).apply {
                    this.setDrawValues(false)
                }

                it.setVisibleXRangeMinimum(10f)
                it.setVisibleXRangeMaximum(50f)

                if (followData) {
                    it.moveViewToAnimated(it.data.xMax, 0f, YAxis.AxisDependency.LEFT, 100)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
        )

        Spacer(Modifier.size(8.dp))

        Card(
            onClick = {
                followData = !followData
            },
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(8.dp)
            ) {
                Text(stringResource(id = R.string.maintain_scroll))

                Spacer(Modifier.weight(1f))

                Checkbox(checked = followData, onCheckedChange = { followData = !followData })
            }
        }
    }
}