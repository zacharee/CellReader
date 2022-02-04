package dev.zwander.cellreader.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Icon
import android.os.Build
import android.telephony.AccessNetworkConstants
import android.telephony.CellSignalStrength
import android.telephony.CellSignalStrengthWcdma
import android.telephony.TelephonyManager
import android.util.Log
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
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
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import dev.zwander.cellreader.BuildConfig
import dev.zwander.cellreader.UpdaterService
import dev.zwander.cellreader.data.ARFCNTools
import dev.zwander.cellreader.data.R
import dev.zwander.cellreader.data.data.CellModel
import dev.zwander.cellreader.data.util.asBitmap
import dev.zwander.cellreader.data.util.asMccMnc
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
                    .appWidgetBackground()
                    .fillMaxSize()
            ) {
                LazyColumn(
                    modifier = GlanceModifier.fillMaxSize()
                ) {
                    sortedSubIds.forEachIndexed { index, t ->
                        item(t.toLong()) {
                            Box(
                                modifier = GlanceModifier.padding(bottom = 4.dp, top = if (index > 0) 4.dp else 0.dp)
                            ) {
                                Column(
                                    modifier = GlanceModifier.wrapContentHeight()
                                        .fillMaxWidth()
                                        .background(ImageProvider(R.drawable.sim_card_widget_background))
                                        .cornerRadius(12.dp)
                                        .padding(bottom = 4.dp, top = 4.dp),
                                    horizontalAlignment = Alignment.Horizontal.CenterHorizontally
                                ) {
                                    val subInfo = subInfos[t]

                                    Row(
                                        verticalAlignment = Alignment.Vertical.CenterVertically
                                    ) {
                                        subInfo?.iconBitmap?.let { bmp ->
                                            Image(
                                                provider = ImageProvider(Icon.createWithData(bmp, 0, bmp.size)),
                                                contentDescription = null,
                                                modifier = GlanceModifier.size(16.dp)
                                            )
                                        }

                                        Spacer(GlanceModifier.size(8.dp))

                                        Text(
                                            text = (subInfo?.carrierName ?: t.toString()),
                                            style = TextStyle(
                                                color = ColorProvider(Color.White)
                                            )
                                        )
                                    }

                                    Spacer(GlanceModifier.size(4.dp))

                                    val rplmn = remember(serviceStates[subInfo?.id]) {
                                        serviceStates[subInfo?.id]?.getNetworkRegistrationInfoListForTransportType(
                                            AccessNetworkConstants.TRANSPORT_TYPE_WWAN
                                        )
                                            ?.firstOrNull { it.accessNetworkTechnology != TelephonyManager.NETWORK_TYPE_IWLAN }
                                            ?.rplmn.asMccMnc
                                    }

                                    Row(
                                        verticalAlignment = Alignment.Vertical.CenterVertically,
                                        modifier = GlanceModifier.fillMaxWidth()
                                    ) {
                                        Spacer(GlanceModifier.defaultWeight())

                                        FormatWidgetText(
                                            name = context.resources.getString(R.string.rplmn_format),
                                            value = rplmn
                                        )

                                        Spacer(GlanceModifier.defaultWeight())

                                        FormatWidgetText(
                                            name = context.resources.getString(R.string.carrier_aggregation_format),
                                            value = serviceStates[subInfo?.id]?.isUsingCarrierAggregation
                                        )

                                        Spacer(GlanceModifier.defaultWeight())
                                    }
                                }
                            }
                        }

                        itemsIndexed(strengthInfos[t]!!, { index, _ -> "$t:$index".hashCode().toLong() }) { index, item ->
                            StrengthCard(
                                strength = item,
                                size = size,
                                modifier = if (index < strengthInfos[t]!!.lastIndex || !cellInfos[t].isNullOrEmpty()) GlanceModifier.padding(bottom = 4.dp) else GlanceModifier
                            )
                        }

                        itemsIndexed(cellInfos[t]!!, { _, item -> "$t:${item.cellIdentity}".hashCode().toLong() }) { index, item ->
                            SignalCard(
                                cellInfo = item, size = size,
                                modifier = if (index < cellInfos[t]!!.lastIndex) GlanceModifier.padding(bottom = 4.dp) else GlanceModifier
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun SignalBarGroup(level: Int, dbm: Int, type: String) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                contentAlignment = Alignment.TopCenter
            ) {
                Text(
                    text = type.first().toString(),
                    style = TextStyle(
                        textAlign = TextAlign.Center,
                        fontSize = 12.sp,
                        color = ColorProvider(Color.White)
                    ),
                    modifier = GlanceModifier.padding(end = 14.dp)
                )

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
            }

            Text(
                text = dbm.toString(),
                style = TextStyle(
                    fontSize = 12.sp,
                    color = ColorProvider(Color.White)
                )
            )
        }
    }

    private fun CellSignalStrengthWrapper.createItems(context: Context, full: Boolean = true): Map<String, Any?> {
        return hashMapOf<String, Any?>().apply {
            when {
                this@createItems is CellSignalStrengthLteWrapper -> {
                    put(
                        context.resources.getString(R.string.rsrq_format),
                        rsrq
                    )
                }
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && this@createItems is CellSignalStrengthNrWrapper -> {
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

            if (full) {
                put(
                    context.resources.getString(R.string.asu_format),
                    asuLevel
                )
            }
        }
    }

    private fun CellInfoWrapper.createItems(context: Context): Map<String, Any?> {
        return hashMapOf<String, Any?>().apply {
            with (cellSignalStrength) {
                putAll(this.createItems(context, false))
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

                if (!plmn.isNullOrBlank()) {
                    put(
                        context.resources.getString(R.string.plmn_format),
                        plmn.asMccMnc
                    )
                }
            }
        }
    }

    @Composable
    private fun BaseCard(strength: CellSignalStrengthWrapper, size: DpSize, modifier: GlanceModifier, backgroundResource: Int, items: Map<String, Any?>) {
        val context = LocalContext.current

        Box(
            modifier = modifier
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = GlanceModifier.fillMaxWidth()
                    .background(imageProvider = ImageProvider(backgroundResource))
                    .cornerRadius(12.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = GlanceModifier.padding(start = 8.dp, end = 8.dp)
                        .fillMaxWidth(),
                ) {
                    val type = remember(strength) {
                        context.resources.getString(
                            strength.run {
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
                                Spacer(GlanceModifier.defaultWeight())
                                columns.forEachIndexed { index, column ->
                                    FormatWidgetText(name = column.first, value = column.second)

                                    if (index < columns.lastIndex) {
                                        Spacer(GlanceModifier.defaultWeight())
                                    }
                                }
                                Spacer(GlanceModifier.defaultWeight())
                            }
                        }
                    }

                    if (size.width >= 190.dp) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            SignalBarGroup(level = strength.level, dbm = strength.dbm, type = type)

                            Spacer(GlanceModifier.size(8.dp))

                            Column(
                                modifier = GlanceModifier.fillMaxWidth()
                            ) {
                                itemGrid()
                            }
                        }
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = GlanceModifier.padding(top = 4.dp, bottom = 4.dp)
                        ) {
                            SignalBarGroup(level = strength.level, dbm = strength.dbm, type = type)

                            Spacer(GlanceModifier.size(8.dp))

                            itemGrid()
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun SignalCard(cellInfo: CellInfoWrapper, size: DpSize, modifier: GlanceModifier) {
        val context = LocalContext.current

        val items = remember(cellInfo) {
            cellInfo.createItems(context)
        }

        BaseCard(strength = cellInfo.cellSignalStrength, size = size, modifier = modifier,
            backgroundResource = R.drawable.signal_card_widget_background, items = items)
    }

    @Composable
    private fun StrengthCard(strength: CellSignalStrengthWrapper, size: DpSize, modifier: GlanceModifier) {
        val context = LocalContext.current

        val items = remember(strength) {
            strength.createItems(context)
        }

        BaseCard(
            strength = strength,
            size = size,
            modifier = modifier,
            backgroundResource = R.drawable.signal_strength_widget_background,
            items = items
        )
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