package dev.zwander.cellreader.data.util

import android.content.Context
import androidx.core.graphics.toColorInt
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import dev.zwander.cellreader.data.R
import dev.zwander.cellreader.data.data.CellModel
import dev.zwander.cellreader.data.data.GraphInfo
import dev.zwander.cellreader.data.data.GraphLineInfo
import dev.zwander.cellreader.data.typeString
import dev.zwander.cellreader.data.wrappers.CellSignalStrengthLteWrapper
import dev.zwander.cellreader.data.wrappers.CellSignalStrengthNrWrapper

fun populatePoints(context: Context) {
    val strengthPoints = HashMap(CellModel.strengthPoints.value ?: mapOf())

    CellModel.strengthInfos.forEach { (subId, infos) ->
        if (!strengthPoints.containsKey(subId)) {
            strengthPoints[subId] = GraphInfo(subId)
        }

        infos.forEach { info ->
            val typeString = info.typeString(context)

            context.resources.getString(R.string.legend_strength, subId, typeString).let { label ->
                if (!strengthPoints[subId]!!.lines.containsKey(label)) {
                    val color = label.toColorString().toColorInt()
                    strengthPoints[subId]!!.lines[label] = GraphLineInfo(
                        subId,
                        label,
                        color
                    )
                }

                strengthPoints[subId]!!.lines[label]!!.apply {
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
                    if (!strengthPoints[subId]!!.lines.containsKey(label)) {
                        val color = label.toColorString().toColorInt()
                        strengthPoints[subId]!!.lines[label] = GraphLineInfo(
                            subId,
                            label,
                            color
                        )
                    }

                    strengthPoints[subId]!!.lines[label]!!.apply {
                        line.add(
                            Entry(
                                line.size.toFloat(),
                                rssi.toFloat()
                            )
                        )
                    }
                }

                context.resources.getString(R.string.legend_rsrq, subId, typeString).let { label ->
                    if (!strengthPoints[subId]!!.lines.containsKey(label)) {
                        val color = label.toColorString().toColorInt()
                        strengthPoints[subId]!!.lines[label] = GraphLineInfo(
                            subId,
                            label,
                            color,
                            axis = YAxis.AxisDependency.RIGHT
                        )
                    }

                    strengthPoints[subId]!!.lines[label]!!.apply {
                        line.add(
                            Entry(
                                line.size.toFloat(),
                                rsrq.toFloat()
                            )
                        )
                    }
                }

                context.resources.getString(R.string.legend_rssnr, subId, typeString).let { label ->
                    if (!strengthPoints[subId]!!.lines.containsKey(label)) {
                        val color = label.toColorString().toColorInt()
                        strengthPoints[subId]!!.lines[label] = GraphLineInfo(
                            subId,
                            label,
                            color,
                            axis = YAxis.AxisDependency.RIGHT
                        )
                    }

                    strengthPoints[subId]!!.lines[label]!!.apply {
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
                        if (!strengthPoints[subId]!!.lines.containsKey(label)) {
                            val color = label.toColorString().toColorInt()
                            strengthPoints[subId]!!.lines[label] = GraphLineInfo(
                                subId,
                                label,
                                color
                            )
                        }

                        strengthPoints[subId]!!.lines[label]!!.apply {
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
                        if (!strengthPoints[subId]!!.lines.containsKey(label)) {
                            val color = label.toColorString().toColorInt()
                            strengthPoints[subId]!!.lines[label] = GraphLineInfo(
                                subId,
                                label,
                                color
                            )
                        }

                        strengthPoints[subId]!!.lines[label]!!.apply {
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
                        if (!strengthPoints[subId]!!.lines.containsKey(label)) {
                            val color = label.toColorString().toColorInt()
                            strengthPoints[subId]!!.lines[label] = GraphLineInfo(
                                subId,
                                label,
                                color,
                                axis = YAxis.AxisDependency.RIGHT
                            )
                        }

                        strengthPoints[subId]!!.lines[label]!!.apply {
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
                        if (!strengthPoints[subId]!!.lines.containsKey(label)) {
                            val color = label.toColorString().toColorInt()
                            strengthPoints[subId]!!.lines[label] = GraphLineInfo(
                                subId,
                                label,
                                color,
                                axis = YAxis.AxisDependency.RIGHT
                            )
                        }

                        strengthPoints[subId]!!.lines[label]!!.apply {
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
                        if (!strengthPoints[subId]!!.lines.containsKey(label)) {
                            val color = label.toColorString().toColorInt()
                            strengthPoints[subId]!!.lines[label] = GraphLineInfo(
                                subId,
                                label,
                                color,
                                axis = YAxis.AxisDependency.RIGHT
                            )
                        }

                        strengthPoints[subId]!!.lines[label]!!.apply {
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
                        if (!strengthPoints[subId]!!.lines.containsKey(label)) {
                            val color = label.toColorString().toColorInt()
                            strengthPoints[subId]!!.lines[label] = GraphLineInfo(
                                subId,
                                label,
                                color,
                                axis = YAxis.AxisDependency.RIGHT
                            )
                        }

                        strengthPoints[subId]!!.lines[label]!!.apply {
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

    CellModel.strengthPoints.value = strengthPoints
}