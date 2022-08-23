package dev.zwander.cellreader.widget

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.telephony.AccessNetworkConstants
import android.telephony.TelephonyManager
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.glance.*
import androidx.glance.action.*
import androidx.glance.appwidget.*
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.itemsIndexed
import androidx.glance.layout.*
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import dev.zwander.cellreader.MainActivity
import dev.zwander.cellreader.UpdaterService
import dev.zwander.cellreader.data.ARFCNTools
import dev.zwander.cellreader.data.R
import dev.zwander.cellreader.data.data.CellModel
import dev.zwander.cellreader.data.layouts.glance.SignalBarGroup
import dev.zwander.cellreader.data.typeString
import dev.zwander.cellreader.data.util.asMccMnc
import dev.zwander.cellreader.data.util.onAvail
import dev.zwander.cellreader.data.wrappers.*
import java.io.File
import java.util.*

class SinglePreferenceGlanceStateDefinition(private val constantKey: String) : GlanceStateDefinition<Preferences> {
    companion object {
        @Volatile
        private var store: DataStore<Preferences>? = null
    }

    override fun getLocation(context: Context, fileKey: String): File {
        return PreferencesGlanceStateDefinition.getLocation(context, constantKey)
    }

    override suspend fun getDataStore(context: Context, fileKey: String): DataStore<Preferences> {
        return store ?: PreferencesGlanceStateDefinition.getDataStore(context, constantKey).apply {
            store = this
        }
    }
}

class SignalWidget : GlanceAppWidget() {
//    companion object {
//        val PRIMARY_CELL_KEY = intPreferencesKey("PRIMARY_CELL")
//        val SUB_IDS_KEY = stringSetPreferencesKey("SUB_IDS")
//        val CELL_INFOS_KEY = stringPreferencesKey("CELL_INFOS")
//        val STRENGTH_INFOS_KEY = stringPreferencesKey("STRENGTH_INFOS")
//        val SUB_INFOS_KEY = stringPreferencesKey("SUB_INFOS")
//        val SERVICE_STATES_KEY = stringPreferencesKey("SERVICE_STATES")
//        val SIGNAL_STRENGTHS_KEY = stringPreferencesKey("SIGNAL_STRENGTHS")
//    }

    override val sizeMode: SizeMode = SizeMode.Exact

    override val stateDefinition = SinglePreferenceGlanceStateDefinition("WIDGET_OPTIONS")

    @SuppressLint("InlinedApi")
    @Composable
    override fun Content() {
        val context = LocalContext.current
        val size = LocalSize.current

//        val betweenUtils = BetweenUtils.getInstance(context)
//
//        val subIds = currentState(SUB_IDS_KEY)?.map { it.toInt() } ?: listOf()
//        val subInfos = betweenUtils.otherGson.fromJson<HashMap<Int, SubscriptionInfoWrapper?>>(
//            currentState(SUB_INFOS_KEY) ?: "",
//            object : TypeToken<HashMap<Int, SubscriptionInfoWrapper?>>(){}.type
//        ) ?: hashMapOf()
//        val serviceStates = betweenUtils.serviceStateGson.fromJson<HashMap<Int, ServiceStateWrapper?>>(
//            currentState(SERVICE_STATES_KEY) ?: "",
//            object : TypeToken<HashMap<Int, ServiceStateWrapper?>>(){}.type
//        ) ?: hashMapOf()
//        val strengthInfos = betweenUtils.cellSignalStrengthGson.fromJson<HashMap<Int, ArrayList<CellSignalStrengthWrapper>>>(
//            currentState(STRENGTH_INFOS_KEY) ?: "",
//            object : TypeToken<HashMap<Int, ArrayList<CellSignalStrengthWrapper>>>(){}.type
//        ) ?: hashMapOf()
//        val cellInfos = betweenUtils.cellInfoGson.fromJson<HashMap<Int, ArrayList<CellInfoWrapper>>>(
//            currentState(CELL_INFOS_KEY) ?: "",
//            object : TypeToken<HashMap<Int, ArrayList<CellInfoWrapper>>>(){}.type
//        ) ?: hashMapOf()

        val subIds = CellModel.subIds.value ?: TreeSet()
        val subInfos = CellModel.subInfos.value ?: hashMapOf()
        val serviceStates = CellModel.serviceStates.value ?: hashMapOf()
        val strengthInfos = CellModel.strengthInfos.value ?: hashMapOf()
        val cellInfos = CellModel.cellInfos.value ?: hashMapOf()

        Box(
            modifier = GlanceModifier.cornerRadius(8.dp)
                .appWidgetBackground()
                .fillMaxSize(),
        ) {
            // We need this text so the widget actually updates.
            // If the LazyColumn is first, updates aren't always rendered.
            // TODO: Revisit this once Glance is more stable.
            Text("")
            LazyColumn(
                modifier = GlanceModifier.fillMaxSize()
            ) {
                subIds.forEachIndexed { _, t ->
                    item(t.toLong()) {
                        Box(
                            modifier = GlanceModifier.padding(bottom = 4.dp)
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
                                    subInfo?.iconBitmapBmp?.let { bmp ->
                                        Image(
                                            provider = ImageProvider(bmp),
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

                                val rplmn =
                                    serviceStates[subInfo?.id]?.getNetworkRegistrationInfoListForTransportType(
                                        AccessNetworkConstants.TRANSPORT_TYPE_WWAN
                                    )
                                        ?.firstOrNull { it.accessNetworkTechnology != TelephonyManager.NETWORK_TYPE_IWLAN }
                                        ?.rplmn.asMccMnc

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

                    itemsIndexed(
                        strengthInfos[t]!!,
                        { index, _ -> "$t:$index".hashCode().toLong() }) { _, item ->
                        StrengthCard(
                            strength = item,
                            size = size,
                            modifier = GlanceModifier.padding(bottom = 4.dp)
                        )
                    }

                    itemsIndexed(
                        cellInfos[t]!!,
                        { _, item ->
                            "$t:${item.cellIdentity}".hashCode().toLong()
                        }) { _, item ->
                        SignalCard(
                            cellInfo = item, size = size,
                            modifier = GlanceModifier.padding(bottom = 4.dp)
                        )
                    }
                }

                item {
                    Box(
                        modifier = GlanceModifier.cornerRadius(12.dp)
                    ) {
                        Box(
                            modifier = GlanceModifier.fillMaxWidth()
                                .background(ImageProvider(R.drawable.open_app_widget_background))
                                .cornerRadius(12.dp)
                                .padding(bottom = 8.dp, top = 8.dp)
                                .clickable(onClick = actionStartActivity<MainActivity>()),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = context.resources.getString(R.string.open_app),
                                style = TextStyle(
                                    color = ColorProvider(Color.White)
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    private fun CellSignalStrengthWrapper.createItems(
        context: Context,
        full: Boolean = true
    ): Map<String, Any?> {
        val map = hashMapOf<String, Any?>()

        when {
            this@createItems is CellSignalStrengthLteWrapper -> {
                map[context.resources.getString(R.string.rsrq_format)] = rsrq

                if (full) {
                    map[context.resources.getString(R.string.rssi_format)] = rssi
                }
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && this@createItems is CellSignalStrengthNrWrapper -> {
                csiRsrq.onAvail {
                    map[context.resources.getString(R.string.rsrq_format)] = it
                }

                ssRsrq.onAvail {
                    map[context.resources.getString(R.string.rsrq_format)] = it
                }
            }
            else -> {}
        }

        if (full) {
            map[context.resources.getString(R.string.asu_format)] = asuLevel
        }

        return map
    }

    private fun CellInfoWrapper.createItems(context: Context): Map<String, Any?> {
        val map = hashMapOf<String, Any?>()

        with(cellSignalStrength) {
            map.putAll(this.createItems(context, false))
        }

        with(cellIdentity) {
            when {
                this is CellIdentityGsmWrapper -> {
                    val arfcnInfo = ARFCNTools.gsmArfcnToInfo(arfcn)
                    val bands = arfcnInfo.map { it.band }

                    if (bands.isNotEmpty()) {
                        map[context.resources.getString(R.string.bands_format)] = bands.joinToString(", ")
                    }
                }
                this is CellIdentityWcdmaWrapper -> {
                    val arfcnInfo = ARFCNTools.gsmArfcnToInfo(uarfcn)
                    val bands = arfcnInfo.map { it.band }

                    if (bands.isNotEmpty()) {
                        map[context.resources.getString(R.string.bands_format)] = bands.joinToString(", ")
                    }
                }
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && this is CellIdentityTdscdmaWrapper -> {
                    val arfcnInfo = ARFCNTools.gsmArfcnToInfo(uarfcn)
                    val bands = arfcnInfo.map { it.band }

                    if (bands.isNotEmpty()) {
                        map[context.resources.getString(R.string.bands_format)] = bands.joinToString(", ")
                    }
                }
                this is CellIdentityLteWrapper -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        map[context.resources.getString(R.string.bands_format)] = bands.joinToString(", ")
                    } else {
                        val arfcnInfo = ARFCNTools.gsmArfcnToInfo(earfcn)
                        val bands = arfcnInfo.map { it.band }

                        if (bands.isNotEmpty()) {
                            map[context.resources.getString(R.string.bands_format)] = bands.joinToString(", ")
                        }
                    }
                }
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && this is CellIdentityNrWrapper -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        map[context.resources.getString(R.string.bands_format)] = bands.joinToString(", ")
                    } else {
                        val arfcnInfo = ARFCNTools.gsmArfcnToInfo(nrArfcn)
                        val bands = arfcnInfo.map { it.band }

                        if (bands.isNotEmpty()) {
                            map[context.resources.getString(R.string.bands_format)] = bands.joinToString(", ")
                        }
                    }
                }
            }

            if (!plmn.isNullOrBlank()) {
                map[context.resources.getString(R.string.plmn_format)] = plmn.asMccMnc
            }
        }

        return map
    }

    @Composable
    private fun BaseCard(
        strength: CellSignalStrengthWrapper,
        size: DpSize,
        modifier: GlanceModifier,
        backgroundResource: Int,
        items: Map<String, Any?>
    ) {
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
                    val type = strength.typeString(context)

                    val itemGridArray = run {
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

                    if (size.width >= 180.dp) {
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

        val items = cellInfo.createItems(context)

        BaseCard(
            strength = cellInfo.cellSignalStrength, size = size, modifier = modifier,
            backgroundResource = R.drawable.signal_card_widget_background, items = items
        )
    }

    @Composable
    private fun StrengthCard(
        strength: CellSignalStrengthWrapper,
        size: DpSize,
        modifier: GlanceModifier
    ) {
        val context = LocalContext.current

        val items = strength.createItems(context)

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
    override val glanceAppWidget = SignalWidget()

    override fun onEnabled(context: Context) {
        super.onEnabled(context)

        context.startForegroundService(Intent(context, UpdaterService::class.java))
    }
}