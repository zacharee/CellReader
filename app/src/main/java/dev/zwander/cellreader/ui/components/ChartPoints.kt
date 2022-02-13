package dev.zwander.cellreader.ui.components

import android.content.Context
import androidx.core.graphics.toColorInt
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import dev.zwander.cellreader.data.GraphInfo
import dev.zwander.cellreader.data.GraphLineInfo
import dev.zwander.cellreader.data.R
import dev.zwander.cellreader.data.data.CellModel
import dev.zwander.cellreader.data.typeString
import dev.zwander.cellreader.data.util.onAvail
import dev.zwander.cellreader.data.util.onCast
import dev.zwander.cellreader.data.util.toColorString
import dev.zwander.cellreader.data.wrappers.CellSignalStrengthLteWrapper
import dev.zwander.cellreader.data.wrappers.CellSignalStrengthNrWrapper

fun populatePoints(points: MutableMap<Int, GraphInfo>, context: Context) {
    CellModel.strengthInfos.forEach { (subId, infos) ->
        if (!points.containsKey(subId)) {
            points[subId] = GraphInfo(subId)
        }

        infos.forEach { info ->
            val typeString = info.typeString(context)

            context.resources.getString(R.string.legend_strength, subId, typeString).let { label ->
                if (!points[subId]!!.lines.containsKey(label)) {
                    val color = label.toColorString().toColorInt()
                    points[subId]!!.lines[label] = GraphLineInfo(
                        subId,
                        label,
                        color
                    )
                }

                points[subId]!!.lines[label]!!.apply {
                    line.add(
                        Entry(
                            line.size.toFloat(),
                            info.dbm.toFloat()
                        )
                    )
                }
            }

            info.onCast<CellSignalStrengthLteWrapper> {
                context.resources.getString(R.string.legend_rssi, subId, typeString).let { label ->
                    if (!points[subId]!!.lines.containsKey(label)) {
                        val color = label.toColorString().toColorInt()
                        points[subId]!!.lines[label] = GraphLineInfo(
                            subId,
                            label,
                            color
                        )
                    }

                    points[subId]!!.lines[label]!!.apply {
                        line.add(
                            Entry(
                                line.size.toFloat(),
                                rssi.toFloat()
                            )
                        )
                    }
                }

                context.resources.getString(R.string.legend_rsrq, subId, typeString).let { label ->
                    if (!points[subId]!!.lines.containsKey(label)) {
                        val color = label.toColorString().toColorInt()
                        points[subId]!!.lines[label] = GraphLineInfo(
                            subId,
                            label,
                            color,
                            axis = YAxis.AxisDependency.RIGHT
                        )
                    }

                    points[subId]!!.lines[label]!!.apply {
                        line.add(
                            Entry(
                                line.size.toFloat(),
                                rsrq.toFloat()
                            )
                        )
                    }
                }

                context.resources.getString(R.string.legend_rssnr, subId, typeString).let { label ->
                    if (!points[subId]!!.lines.containsKey(label)) {
                        val color = label.toColorString().toColorInt()
                        points[subId]!!.lines[label] = GraphLineInfo(
                            subId,
                            label,
                            color,
                            axis = YAxis.AxisDependency.RIGHT
                        )
                    }

                    points[subId]!!.lines[label]!!.apply {
                        line.add(
                            Entry(
                                line.size.toFloat(),
                                rssnr.toFloat()
                            )
                        )
                    }
                }
            }

            info.onCast<CellSignalStrengthNrWrapper> {
                ssRsrp.onAvail {
                    context.resources.getString(R.string.legend_ss_rsrp, subId, typeString).let { label ->
                        if (!points[subId]!!.lines.containsKey(label)) {
                            val color = label.toColorString().toColorInt()
                            points[subId]!!.lines[label] = GraphLineInfo(
                                subId,
                                label,
                                color
                            )
                        }

                        points[subId]!!.lines[label]!!.apply {
                            line.add(
                                Entry(
                                    line.size.toFloat(),
                                    it.toFloat()
                                )
                            )
                        }
                    }
                }

                csiRsrp.onAvail {
                    context.resources.getString(R.string.legend_csi_rsrp, subId, typeString).let { label ->
                        if (!points[subId]!!.lines.containsKey(label)) {
                            val color = label.toColorString().toColorInt()
                            points[subId]!!.lines[label] = GraphLineInfo(
                                subId,
                                label,
                                color
                            )
                        }

                        points[subId]!!.lines[label]!!.apply {
                            line.add(
                                Entry(
                                    line.size.toFloat(),
                                    it.toFloat()
                                )
                            )
                        }
                    }
                }

                ssRsrq.onAvail {
                    context.resources.getString(R.string.legend_ss_rsrq, subId, typeString).let { label ->
                        if (!points[subId]!!.lines.containsKey(label)) {
                            val color = label.toColorString().toColorInt()
                            points[subId]!!.lines[label] = GraphLineInfo(
                                subId,
                                label,
                                color,
                                axis = YAxis.AxisDependency.RIGHT
                            )
                        }

                        points[subId]!!.lines[label]!!.apply {
                            line.add(
                                Entry(
                                    line.size.toFloat(),
                                    it.toFloat()
                                )
                            )
                        }
                    }
                }

                csiRsrq.onAvail {
                    context.resources.getString(R.string.legend_csi_rsrq, subId, typeString).let { label ->
                        if (!points[subId]!!.lines.containsKey(label)) {
                            val color = label.toColorString().toColorInt()
                            points[subId]!!.lines[label] = GraphLineInfo(
                                subId,
                                label,
                                color,
                                axis = YAxis.AxisDependency.RIGHT
                            )
                        }

                        points[subId]!!.lines[label]!!.apply {
                            line.add(
                                Entry(
                                    line.size.toFloat(),
                                    it.toFloat()
                                )
                            )
                        }
                    }
                }

                ssSinr.onAvail {
                    context.resources.getString(R.string.legend_ss_sinr, subId, typeString).let { label ->
                        if (!points[subId]!!.lines.containsKey(label)) {
                            val color = label.toColorString().toColorInt()
                            points[subId]!!.lines[label] = GraphLineInfo(
                                subId,
                                label,
                                color,
                                axis = YAxis.AxisDependency.RIGHT
                            )
                        }

                        points[subId]!!.lines[label]!!.apply {
                            line.add(
                                Entry(
                                    line.size.toFloat(),
                                    it.toFloat()
                                )
                            )
                        }
                    }
                }

                csiSinr.onAvail {
                    context.resources.getString(R.string.legend_csi_sinr, subId, typeString).let { label ->
                        if (!points[subId]!!.lines.containsKey(label)) {
                            val color = label.toColorString().toColorInt()
                            points[subId]!!.lines[label] = GraphLineInfo(
                                subId,
                                label,
                                color,
                                axis = YAxis.AxisDependency.RIGHT
                            )
                        }

                        points[subId]!!.lines[label]!!.apply {
                            line.add(
                                Entry(
                                    line.size.toFloat(),
                                    it.toFloat()
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}