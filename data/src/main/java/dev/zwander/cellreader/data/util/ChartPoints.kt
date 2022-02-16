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
import dev.zwander.cellreader.data.wrappers.*
import kotlin.math.max

fun populatePoints(strengthPoints: MutableMap<Int, GraphInfo>, context: Context, maxX: Int) {
    CellModel.strengthInfos.forEach { (subId, infos) ->
        if (!strengthPoints.containsKey(subId)) {
            strengthPoints[subId] = GraphInfo(subId)
        }

        infos.forEach { info ->
            val typeString = info.typeString(context)
            val line = strengthPoints[subId]!!

//            addToLine(
//                line.lines,
//                subId,
//                context.resources.getString(R.string.legend_strength, subId, typeString),
//                maxX,
//                info.dbm.toFloat()
//            )

            info.onCast<CellSignalStrengthGsmWrapper> {
                addToLine(
                    line.lines,
                    subId,
                    context.resources.getString(
                        R.string.legend_rssi,
                        subId,
                        typeString
                    ),
                    maxX,
                    rssi.toFloat()
                )
            }

            info.onCast<CellSignalStrengthCdmaWrapper> {
                cdmaDbm.onAvail {
                    addToLine(
                        line.lines,
                        subId,
                        context.resources.getString(
                            R.string.legend_dbm,
                            subId,
                            context.resources.getString(R.string.cdma)
                        ),
                        maxX,
                        cdmaDbm.toFloat()
                    )
                }

                evdoDbm.onAvail {
                    addToLine(
                        line.lines,
                        subId,
                        context.resources.getString(
                            R.string.legend_dbm,
                            subId,
                            context.resources.getString(R.string.evdo)
                        ),
                        maxX,
                        evdoDbm.toFloat()
                    )
                }

                cdmaEcio.onAvail {
                    addToLine(
                        line.lines,
                        subId,
                        context.resources.getString(
                            R.string.legend_ecio,
                            subId,
                            context.resources.getString(R.string.cdma)
                        ),
                        maxX,
                        cdmaEcio.toFloat()
                    )
                }

                evdoEcio.onAvail {
                    addToLine(
                        line.lines,
                        subId,
                        context.resources.getString(
                            R.string.legend_ecio,
                            subId,
                            context.resources.getString(R.string.evdo)
                        ),
                        maxX,
                        evdoEcio.toFloat()
                    )
                }

                evdoSnr.onAvail {
                    addToLine(
                        line.lines,
                        subId,
                        context.resources.getString(
                            R.string.legend_evdo_snr,
                            subId
                        ),
                        maxX,
                        evdoSnr.toFloat(),
                        YAxis.AxisDependency.RIGHT
                    )
                }
            }

            info.onCast<CellSignalStrengthWcdmaWrapper> {
                addToLine(
                    line.lines,
                    subId,
                    context.resources.getString(
                        R.string.legend_rssi,
                        subId,
                        typeString
                    ),
                    maxX,
                    rssi.toFloat()
                )

                addToLine(
                    line.lines,
                    subId,
                    context.resources.getString(
                        R.string.legend_ecno,
                        subId,
                        typeString
                    ),
                    maxX,
                    ecNo.toFloat(),
                    YAxis.AxisDependency.RIGHT
                )

                addToLine(
                    line.lines,
                    subId,
                    context.resources.getString(
                        R.string.legend_rscp,
                        subId,
                        typeString
                    ),
                    maxX,
                    rscp.toFloat()
                )
            }

            info.onCast<CellSignalStrengthTdscdmaWrapper> {
                addToLine(
                    line.lines,
                    subId,
                    context.resources.getString(
                        R.string.legend_rscp,
                        subId,
                        typeString
                    ),
                    maxX,
                    rscp.toFloat()
                )
            }

            info.onCast<CellSignalStrengthLteWrapper> {
                addToLine(
                    line.lines,
                    subId,
                    context.resources.getString(R.string.legend_rsrp, subId, typeString),
                    maxX,
                    rsrp.toFloat()
                )

                addToLine(
                    line.lines,
                    subId,
                    context.resources.getString(R.string.legend_rssi, subId, typeString),
                    maxX,
                    rssi.toFloat()
                )

                addToLine(
                    line.lines,
                    subId,
                    context.resources.getString(R.string.legend_rsrq, subId, typeString),
                    maxX,
                    rsrq.toFloat(),
                    YAxis.AxisDependency.RIGHT
                )

                addToLine(
                    line.lines,
                    subId,
                    context.resources.getString(R.string.legend_rssnr, subId, typeString),
                    maxX,
                    rssnr.toFloat(),
                    YAxis.AxisDependency.RIGHT
                )
            }

            info.onCast<CellSignalStrengthNrWrapper> {
                ssRsrp.onAvail {
                    addToLine(
                        line.lines,
                        subId,
                        context.resources.getString(R.string.legend_ss_rsrp, subId, typeString),
                        maxX,
                        it.toFloat()
                    )
                }

                csiRsrp.onAvail {
                    addToLine(
                        line.lines,
                        subId,
                        context.resources.getString(R.string.legend_csi_rsrp, subId, typeString),
                        maxX,
                        it.toFloat()
                    )
                }

                ssRsrq.onAvail {
                    addToLine(
                        line.lines,
                        subId,
                        context.resources.getString(R.string.legend_ss_rsrq, subId, typeString),
                        maxX,
                        it.toFloat(),
                        YAxis.AxisDependency.RIGHT
                    )
                }

                csiRsrq.onAvail {
                    addToLine(
                        line.lines,
                        subId,
                        context.resources.getString(R.string.legend_csi_rsrq, subId, typeString),
                        maxX,
                        it.toFloat(),
                        YAxis.AxisDependency.RIGHT
                    )
                }

                ssSinr.onAvail {
                    addToLine(
                        line.lines,
                        subId,
                        context.resources.getString(R.string.legend_ss_sinr, subId, typeString),
                        maxX,
                        it.toFloat(),
                        YAxis.AxisDependency.RIGHT
                    )
                }

                csiSinr.onAvail {
                    addToLine(
                        line.lines,
                        subId,
                        context.resources.getString(R.string.legend_csi_sinr, subId, typeString),
                        maxX,
                        it.toFloat(),
                        YAxis.AxisDependency.RIGHT
                    )
                }
            }
        }
    }
}

private fun addToLine(lines: MutableMap<String, GraphLineInfo>, subId: Int, label: String, maxX: Int, value: Float, axis: YAxis.AxisDependency = YAxis.AxisDependency.LEFT) {
    createLineIfNotExists(lines, subId, label, axis)

    lines[label]!!.apply {
        line.add(
            Entry(
                maxX.toFloat(),
                value
            )
        )
    }
}

private fun createLineIfNotExists(lines: MutableMap<String, GraphLineInfo>, subId: Int, label: String, axis: YAxis.AxisDependency = YAxis.AxisDependency.LEFT) {
    if (!lines.containsKey(label)) {
        val color = label.toColorString().toColorInt()
        lines[label] = GraphLineInfo(subId, label, color, axis)
    }
}