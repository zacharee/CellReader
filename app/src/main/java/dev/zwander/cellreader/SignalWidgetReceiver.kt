package dev.zwander.cellreader

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.telephony.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.glance.*
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.itemsIndexed
import androidx.glance.appwidget.updateAll
import androidx.glance.layout.*
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.Text
import dev.zwander.cellreader.data.ARFCNTools
import dev.zwander.cellreader.data.data.CellModel
import dev.zwander.cellreader.data.data.CellModelWear
import dev.zwander.cellreader.data.data.ProvideCellModel
import dev.zwander.cellreader.utils.cellIdentityCompat
import dev.zwander.cellreader.utils.cellSignalStrengthCompat
import dev.zwander.cellreader.utils.onAvail
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class SignalWidget : GlanceAppWidget() {
    override val stateDefinition = PreferencesGlanceStateDefinition

    @Composable
    override fun Content() {
        val context = LocalContext.current

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
                                Text(text = context.resources.getString(R.string.sim_slot_format, t.toString()))
                            }
                        }

                        itemsIndexed(cellInfos[t]!!, { _, item -> "$t:${item.cellIdentityCompat}".hashCode().toLong() }) { _, item ->
                            SignalCard(cellInfo = item)
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun SignalCard(cellInfo: CellInfo) {
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            provider = ImageProvider(
                                when (cellInfo.cellSignalStrengthCompat.level) {
                                    CellSignalStrength.SIGNAL_STRENGTH_POOR -> R.drawable.cell_1
                                    CellSignalStrength.SIGNAL_STRENGTH_MODERATE -> R.drawable.cell_2
                                    CellSignalStrength.SIGNAL_STRENGTH_GOOD -> R.drawable.cell_3
                                    CellSignalStrength.SIGNAL_STRENGTH_GREAT -> R.drawable.cell_4
                                    else -> R.drawable.cell_0
                                }
                            ),
                            contentDescription = null,
                            modifier = GlanceModifier.width(48.dp).height(48.dp),
                            contentScale = ContentScale.Fit
                        )

                        Spacer(GlanceModifier.size(8.dp))

                        Text(text = cellInfo.cellSignalStrengthCompat.dbm.toString())
                    }

                    Spacer(GlanceModifier.size(8.dp))

                    Column(
                        modifier = GlanceModifier.fillMaxWidth()
                    ) {
                        Text(
                            text = context.resources.getString(
                                R.string.type_format,
                                context.resources.getString(
                                    cellInfo.cellSignalStrengthCompat.run {
                                        when {
                                            this is CellSignalStrengthGsm -> R.string.gsm
                                            this is CellSignalStrengthWcdma -> R.string.wcdma
                                            this is CellSignalStrengthCdma -> R.string.cdma
                                            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && this is CellSignalStrengthTdscdma -> R.string.tdscdma
                                            this is CellSignalStrengthLte -> R.string.lte
                                            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && this is CellSignalStrengthNr -> R.string.nr
                                            else -> R.string.unknown
                                        }
                                    }
                                )
                            )
                        )

                        with(cellInfo.cellSignalStrengthCompat) {
                            when {
                                this is CellSignalStrengthLte -> {
                                    Spacer(GlanceModifier.size(8.dp))

                                    Text(
                                        text = context.resources.getString(R.string.rsrq_format, rsrq.toString())
                                    )
                                }
                                Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && this is CellSignalStrengthNr -> {
                                    Spacer(GlanceModifier.size(8.dp))

                                    csiRsrq.onAvail {
                                        Text(text = context.resources.getString(R.string.rsrq_format, it.toString()))
                                    }

                                    ssRsrq.onAvail {
                                        Text(text = context.resources.getString(R.string.rsrq_format, it.toString()))
                                    }
                                }
                            }
                        }

                        with (cellInfo.cellIdentityCompat) {
                            when {
                                this is CellIdentityGsm -> {
                                    val arfcnInfo = remember(arfcn) {
                                        ARFCNTools.gsmArfcnToInfo(arfcn)
                                    }
                                    val bands = remember(arfcn) {
                                        arfcnInfo.map { it.band }
                                    }

                                    if (bands.isNotEmpty()) {
                                        Spacer(GlanceModifier.size(8.dp))

                                        Text(text = context.resources.getString(R.string.bands_format, bands.joinToString(", ")))
                                    }
                                }
                                this is CellIdentityWcdma -> {
                                    val arfcnInfo = remember(uarfcn) {
                                        ARFCNTools.uarfcnToInfo(uarfcn)
                                    }
                                    val bands = remember(uarfcn) {
                                        arfcnInfo.map { it.band }
                                    }

                                    if (bands.isNotEmpty()) {
                                        Spacer(GlanceModifier.size(8.dp))

                                        Text(text = context.resources.getString(R.string.bands_format, bands.joinToString(", ")))
                                    }
                                }
                                Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && this is CellIdentityTdscdma -> {
                                    val arfcnInfo = remember(uarfcn) {
                                        ARFCNTools.tdscdmaArfcnToInfo(uarfcn)
                                    }
                                    val bands = remember(uarfcn) {
                                        arfcnInfo.map { it.band }
                                    }

                                    if (bands.isNotEmpty()) {
                                        Spacer(GlanceModifier.size(8.dp))

                                        Text(text = context.resources.getString(R.string.bands_format, bands.joinToString(", ")))
                                    }
                                }
                                this is CellIdentityLte -> {
                                    Spacer(GlanceModifier.size(8.dp))

                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                        Text(text = context.resources.getString(R.string.bands_format, bands.joinToString(", ")))
                                    } else {
                                        val arfcnInfo = remember(earfcn) {
                                            ARFCNTools.earfcnToInfo(earfcn)
                                        }
                                        val bands = remember(earfcn) {
                                            arfcnInfo.map { it.band }
                                        }

                                        if (bands.isNotEmpty()) {
                                            Spacer(GlanceModifier.size(8.dp))

                                            Text(text = context.resources.getString(R.string.bands_format, bands.joinToString(", ")))
                                        }
                                    }
                                }
                                Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && this is CellIdentityNr -> {
                                    Spacer(GlanceModifier.size(8.dp))

                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                        Text(text = context.resources.getString(R.string.bands_format, bands.joinToString(", ")))
                                    } else {
                                        val arfcnInfo = remember(nrarfcn) {
                                            ARFCNTools.nrArfcnToInfo(nrarfcn)
                                        }
                                        val bands = remember(nrarfcn) {
                                            arfcnInfo.map { it.band }
                                        }

                                        if (bands.isNotEmpty()) {
                                            Spacer(GlanceModifier.size(8.dp))

                                            Text(text = context.resources.getString(R.string.bands_format, bands.joinToString(", ")))
                                        }
                                    }
                                }
                            }
                        }
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