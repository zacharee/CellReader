package dev.zwander.cellreader.data.util

import android.content.Context
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.utils.Utils
import dev.zwander.cellreader.data.R
import dev.zwander.cellreader.data.data.CellModelBase
import dev.zwander.cellreader.data.data.GraphInfo
import dev.zwander.cellreader.data.data.GraphLineInfo
import dev.zwander.cellreader.data.wrappers.CellSignalStrengthCdmaWrapper
import dev.zwander.cellreader.data.wrappers.CellSignalStrengthGsmWrapper
import dev.zwander.cellreader.data.wrappers.CellSignalStrengthLteWrapper
import dev.zwander.cellreader.data.wrappers.CellSignalStrengthNrWrapper
import dev.zwander.cellreader.data.wrappers.CellSignalStrengthTdscdmaWrapper
import dev.zwander.cellreader.data.wrappers.CellSignalStrengthWcdmaWrapper
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.TreeMap

fun populatePoints(model: CellModelBase, strengthPoints: MutableMap<Int, GraphInfo>, context: Context, maxX: Int) {
    Utils.init(context)
    
    model.strengthInfos.value.forEach { (subId, infos) ->
        if (!strengthPoints.containsKey(subId)) {
            strengthPoints[subId] = GraphInfo(subId)
        }
        
        val simSlot = model.subInfos.value[subId]?.simSlotIndex?.plus(1) ?: subId

        infos.forEach { info ->
            val typeString = with (info.type) { context.label }
            val line = strengthPoints[subId]!!

            info.onCast<CellSignalStrengthGsmWrapper> {
                addToLine(
                    line.lines,
                    subId,
                    context.resources.getString(
                        R.string.legend_rssi,
                        simSlot,
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
                            simSlot,
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
                            simSlot,
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
                            simSlot,
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
                            simSlot,
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
                            simSlot
                        ),
                        maxX,
                        evdoSnr.toFloat(),
                        YAxis.AxisDependency.RIGHT
                    )
                }
            }

            info.onCast<CellSignalStrengthWcdmaWrapper> {
                rssi.onAvail {
                    addToLine(
                        line.lines,
                        subId,
                        context.resources.getString(
                            R.string.legend_rssi,
                            simSlot,
                            typeString
                        ),
                        maxX,
                        rssi.toFloat()
                    )
                }

                ecNo.onAvail {
                    addToLine(
                        line.lines,
                        subId,
                        context.resources.getString(
                            R.string.legend_ecno,
                            simSlot,
                            typeString
                        ),
                        maxX,
                        ecNo.toFloat(),
                        YAxis.AxisDependency.RIGHT
                    )
                }

                rscp.onAvail {
                    addToLine(
                        line.lines,
                        subId,
                        context.resources.getString(
                            R.string.legend_rscp,
                            simSlot,
                            typeString
                        ),
                        maxX,
                        rscp.toFloat()
                    )
                }
            }

            info.onCast<CellSignalStrengthTdscdmaWrapper> {
                addToLine(
                    line.lines,
                    subId,
                    context.resources.getString(
                        R.string.legend_rscp,
                        simSlot,
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
                    context.resources.getString(R.string.legend_rsrp, simSlot, typeString),
                    maxX,
                    rsrp.toFloat()
                )

                addToLine(
                    line.lines,
                    subId,
                    context.resources.getString(R.string.legend_rssi, simSlot, typeString),
                    maxX,
                    rssi.toFloat()
                )

                addToLine(
                    line.lines,
                    subId,
                    context.resources.getString(R.string.legend_rsrq, simSlot, typeString),
                    maxX,
                    rsrq.toFloat(),
                    YAxis.AxisDependency.RIGHT
                )

                addToLine(
                    line.lines,
                    subId,
                    context.resources.getString(R.string.legend_rssnr, simSlot, typeString),
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
                        context.resources.getString(R.string.legend_ss_rsrp, simSlot, typeString),
                        maxX,
                        it.toFloat()
                    )
                }

                csiRsrp.onAvail {
                    addToLine(
                        line.lines,
                        subId,
                        context.resources.getString(R.string.legend_csi_rsrp, simSlot, typeString),
                        maxX,
                        it.toFloat()
                    )
                }

                ssRsrq.onAvail {
                    addToLine(
                        line.lines,
                        subId,
                        context.resources.getString(R.string.legend_ss_rsrq, simSlot, typeString),
                        maxX,
                        it.toFloat(),
                        YAxis.AxisDependency.RIGHT
                    )
                }

                csiRsrq.onAvail {
                    addToLine(
                        line.lines,
                        subId,
                        context.resources.getString(R.string.legend_csi_rsrq, simSlot, typeString),
                        maxX,
                        it.toFloat(),
                        YAxis.AxisDependency.RIGHT
                    )
                }

                ssSinr.onAvail {
                    addToLine(
                        line.lines,
                        subId,
                        context.resources.getString(R.string.legend_ss_sinr, simSlot, typeString),
                        maxX,
                        it.toFloat(),
                        YAxis.AxisDependency.RIGHT
                    )
                }

                csiSinr.onAvail {
                    addToLine(
                        line.lines,
                        subId,
                        context.resources.getString(R.string.legend_csi_sinr, simSlot, typeString),
                        maxX,
                        it.toFloat(),
                        YAxis.AxisDependency.RIGHT
                    )
                }
            }
        }
    }
}

private fun addToLine(lines: MutableStateFlow<MutableMap<String, GraphLineInfo>>, subId: Int, label: String, maxX: Int, value: Float, axis: YAxis.AxisDependency = YAxis.AxisDependency.LEFT) {
    val linesValue = TreeMap(lines.value)

    createLineIfNotExists(linesValue, subId, label, axis)

    linesValue[label]!!.apply {
        line.add(
            Entry(
                maxX.toFloat(),
                value
            )
        )
    }

    lines.value = linesValue
}

private fun createLineIfNotExists(lines: MutableMap<String, GraphLineInfo>, subId: Int, label: String, axis: YAxis.AxisDependency = YAxis.AxisDependency.LEFT) {
    if (!lines.containsKey(label)) {
        val color = label.toColorString().toLightEnoughColorInt()
        lines[label] = GraphLineInfo(subId, label, color, axis)
    }
}