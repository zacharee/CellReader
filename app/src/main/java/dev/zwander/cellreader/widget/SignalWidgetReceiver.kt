package dev.zwander.cellreader.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.telephony.*
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.appwidget.*
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.itemsIndexed
import androidx.glance.layout.*
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import dev.zwander.cellreader.BuildConfig
import dev.zwander.cellreader.UpdaterService
import dev.zwander.cellreader.data.ARFCNTools
import dev.zwander.cellreader.data.data.CellModel
import dev.zwander.cellreader.data.R
import dev.zwander.cellreader.data.util.onAvail
import dev.zwander.cellreader.data.wrappers.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class SignalWidget : GlanceAppWidget() {
    override val stateDefinition = PreferencesGlanceStateDefinition

    override val sizeMode: SizeMode = SizeMode.Exact

    @Composable
    override fun Content() {
        val context = LocalContext.current
        val size = LocalSize.current

        with (CellModel) {
            Box(
                modifier = GlanceModifier.cornerRadius(8.dp)
                    .background(Color(0xff121212))
                    .fillMaxSize()
            ) {
                LazyColumn(
                    modifier = GlanceModifier.fillMaxSize()
                ) {
                    sortedSubIds.forEach { t ->
                        item(t.toLong()) {
                            Box(
                                modifier = GlanceModifier.height(48.dp)
                                    .fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                FormatWidgetText(
                                    name = context.resources.getString(R.string.sim_slot_format),
                                    value = t
                                )
                            }
                        }

                        itemsIndexed(cellInfos[t]!!, { _, item -> "$t:${item.cellIdentity}".hashCode().toLong() }) { _, item ->
                            SignalCard(cellInfo = item, size = size)
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun SignalBarGroup(level: Int, dbm: Int) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                provider = ImageProvider(
                    when (level) {
                        CellSignalStrength.SIGNAL_STRENGTH_POOR -> R.drawable.cell_1
                        CellSignalStrength.SIGNAL_STRENGTH_MODERATE -> R.drawable.cell_2
                        CellSignalStrength.SIGNAL_STRENGTH_GOOD -> R.drawable.cell_3
                        CellSignalStrength.SIGNAL_STRENGTH_GREAT -> R.drawable.cell_4
                        else -> R.drawable.cell_0
                    }
                ),
                contentDescription = null,
                modifier = GlanceModifier.size(32.dp),
                contentScale = ContentScale.Fit,
            )

            Text(
                text = dbm.toString(),
                style = TextStyle(
                    fontSize = 12.sp
                )
            )
        }
    }

    private fun CellInfoWrapper.createItems(context: Context): Map<String, Any?> {
        return hashMapOf<String, Any?>().apply {
            put(
                context.resources.getString(
                    R.string.type_format
                ),
                context.resources.getString(
                    cellSignalStrength.run {
                        when {
                            this is CellSignalStrengthGsmWrapper -> R.string.gsm
                            this is CellSignalStrengthWcdmaWrapper -> R.string.wcdma
                            this is CellSignalStrengthCdmaWrapper -> R.string.cdma
                            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && this is CellSignalStrengthTdscdmaWrapper -> R.string.tdscdma
                            this is CellSignalStrengthLteWrapper -> R.string.lte
                            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && this is CellSignalStrengthNrWrapper -> R.string.nr
                            else -> R.string.unknown
                        }
                    }
                )
            )

            with (cellSignalStrength) {
                when {
                    this is CellSignalStrengthLteWrapper -> {
                        put(
                            context.resources.getString(R.string.rsrq_format),
                            rsrq
                        )
                    }
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && this is CellSignalStrengthNrWrapper -> {
                        csiRsrq.onAvail {
                            put(
                                context.resources.getString(R.string.rsrq_format),
                                it
                            )
                        }

                        ssRsrq.onAvail {
                            put(
                                context.resources.getString(R.string.rsrq_format),
                                it
                            )
                        }
                    }
                    else -> {}
                }
            }

            with (cellIdentity) {
                when {
                    this is CellIdentityGsmWrapper -> {
                        val arfcnInfo = ARFCNTools.gsmArfcnToInfo(arfcn)
                        val bands = arfcnInfo.map { it.band }

                        if (bands.isNotEmpty()) {
                            put(
                                context.resources.getString(R.string.bands_format),
                                bands.joinToString(", ")
                            )
                        }
                    }
                    this is CellIdentityWcdmaWrapper -> {
                        val arfcnInfo = ARFCNTools.gsmArfcnToInfo(uarfcn)
                        val bands = arfcnInfo.map { it.band }

                        if (bands.isNotEmpty()) {
                            put(
                                context.resources.getString(R.string.bands_format),
                                bands.joinToString(", ")
                            )
                        }
                    }
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && this is CellIdentityTdscdmaWrapper -> {
                        val arfcnInfo = ARFCNTools.gsmArfcnToInfo(uarfcn)
                        val bands = arfcnInfo.map { it.band }

                        if (bands.isNotEmpty()) {
                            put(
                                context.resources.getString(R.string.bands_format),
                                bands.joinToString(", ")
                            )
                        }
                    }
                    this is CellIdentityLteWrapper -> {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            put(
                                context.resources.getString(R.string.bands_format),
                                bands?.joinToString(", ")
                            )
                        } else {
                            val arfcnInfo = ARFCNTools.gsmArfcnToInfo(earfcn)
                            val bands = arfcnInfo.map { it.band }

                            if (bands.isNotEmpty()) {
                                put(
                                    context.resources.getString(R.string.bands_format),
                                    bands.joinToString(", ")
                                )
                            }
                        }
                    }
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && this is CellIdentityNrWrapper -> {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            put(
                                context.resources.getString(R.string.bands_format),
                                bands?.joinToString(", ")
                            )
                        } else {
                            val arfcnInfo = ARFCNTools.gsmArfcnToInfo(nrArfcn)
                            val bands = arfcnInfo.map { it.band }

                            if (bands.isNotEmpty()) {
                                put(
                                    context.resources.getString(R.string.bands_format),
                                    bands.joinToString(", ")
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun SignalCard(cellInfo: CellInfoWrapper, size: DpSize) {
        val context = LocalContext.current

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = GlanceModifier.fillMaxWidth()
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = GlanceModifier.padding(8.dp)
                    .fillMaxWidth(),
            ) {
                val items = remember(cellInfo) {
                    cellInfo.createItems(context)
                }
                val itemGridArray by derivedStateOf {
                    val grid = hashMapOf<Int, MutableList<Pair<String, Any?>>>()
                    val rowSize = 3

                    items.entries.forEachIndexed { index, entry ->
                        val gridRowIndex = index / rowSize
                        val gridColumnIndex = index % rowSize

                        if (!grid.containsKey(gridRowIndex)) {
                            grid[gridRowIndex] = mutableListOf()
                        }

                        grid[gridRowIndex]?.add(gridColumnIndex, entry.toPair())
                    }

                    grid
                }

                @Composable
                fun itemGrid() {
                    itemGridArray.forEach { (_, columns) ->
                        Row(
                            modifier = GlanceModifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            columns.forEachIndexed { index, column ->
                                FormatWidgetText(name = column.first, value = column.second)

                                if (index < columns.lastIndex) {
                                    Spacer(GlanceModifier.defaultWeight())
                                }
                            }
                        }
                    }
                }

                if (size.width >= 190.dp) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        SignalBarGroup(level = cellInfo.cellSignalStrength.level, dbm = cellInfo.cellSignalStrength.dbm)

                        Spacer(GlanceModifier.size(16.dp))

                        Column(
                            modifier = GlanceModifier.fillMaxWidth()
                        ) {
                            itemGrid()
                        }
                    }
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        SignalBarGroup(level = cellInfo.cellSignalStrength.level, dbm = cellInfo.cellSignalStrength.dbm)

                        Spacer(GlanceModifier.size(8.dp))

                        itemGrid()
                    }
                }
            }
        }
    }
}

class SignalWidgetReceiver : GlanceAppWidgetReceiver() {
    companion object {
        const val ACTION_REFRESH = "${BuildConfig.APPLICATION_ID}.REFRESH"
    }

    override val glanceAppWidget = SignalWidget()

    override fun onEnabled(context: Context) {
        super.onEnabled(context)

        context.startForegroundService(Intent(context, UpdaterService::class.java))
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_REFRESH) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName =
                ComponentName(context.packageName, checkNotNull(javaClass.canonicalName))
            onUpdate(
                context,
                appWidgetManager,
                appWidgetManager.getAppWidgetIds(componentName)
            )
            GlobalScope.launch {
                glanceAppWidget.updateAll(context)
            }
            return
        }

        super.onReceive(context, intent)
    }
}