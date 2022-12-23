package dev.zwander.cellreader.wear

import android.annotation.SuppressLint
import android.graphics.Point
import android.util.DisplayMetrics
import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.layout.*
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import androidx.glance.wear.tiles.GlanceTileService
import androidx.wear.tiles.EventBuilders
import dev.zwander.cellreader.data.R
import dev.zwander.cellreader.data.data.CellModelWear
import dev.zwander.cellreader.data.layouts.glance.SignalBarGroup
import dev.zwander.cellreader.data.typeString
import kotlinx.coroutines.*

class CellTile : GlanceTileService(), CoroutineScope by MainScope() {
    data class TileItemInfo(
        val level: Int,
        val dBm: Int,
        val bands: List<String>,
        val type: String,
        val subId: Int
    )

    private val wm by lazy { getSystemService(WINDOW_SERVICE) as WindowManager }

    override fun onTileEnterEvent(requestParams: EventBuilders.TileEnterEvent) {
        launch {
            DataHandler.addHandle(this@CellTile, this@CellTile)
        }
    }

    override fun onTileLeaveEvent(requestParams: EventBuilders.TileLeaveEvent) {
        launch {
            DataHandler.removeHandle(this@CellTile, this@CellTile)
        }
    }

    @SuppressLint("StateFlowValueCalledInComposition")
    @Composable
    override fun Content() {
        val sizePx = Point().apply {
            @Suppress("DEPRECATION")
            wm.defaultDisplay.getSize(this)
        }
        val sizeDp = sizePx.let { px -> DisplayMetrics().run {
            @Suppress("DEPRECATION")
            wm.defaultDisplay.getMetrics(this)

            DpSize((px.x / this.density).dp, (px.y / this.density).dp)
        } }

        val cellModel = CellModelWear.getInstance()
        val subIds = cellModel.subIds.value
        val cellInfos = cellModel.cellInfos.value
        val strengthInfos = cellModel.strengthInfos.value

        val items = subIds.map { subId ->
            val cellInfo = cellInfos[subId]
            val strength = strengthInfos[subId]

            cellInfo?.get(0)?.let {
                TileItemInfo(
                    it.cellSignalStrength.level,
                    it.cellSignalStrength.dbm,
                    it.cellIdentity.bands,
                    it.cellIdentity.typeString(this@CellTile),
                    subId
                )
            } to strength?.get(0)?.let {
                TileItemInfo(
                    it.level,
                    it.dbm,
                    listOf(),
                    it.typeString(this@CellTile),
                    subId
                )
            }
        }

        ItemGrid(items = items, GlanceModifier.width(sizeDp.width).height(sizeDp.height))
    }

    @Composable
    private fun ItemGrid(items: List<Pair<TileItemInfo?, TileItemInfo?>>, modifier: GlanceModifier) {
        Row(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier
        ) {
            items.forEachIndexed { index, (first, second) ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = GlanceModifier
                ) {
                    if (first != null) {
                        Item(first, GlanceModifier)
                    }

                    Text("${(first ?: second)!!.subId}")

                    if (second != null) {
                        Item(second, GlanceModifier)
                    }
                }

                if (index < items.lastIndex) {
                    Spacer(GlanceModifier.size(8.dp))
                }
            }
        }
    }

    @Composable
    private fun Item(info: TileItemInfo, modifier: GlanceModifier) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier
        ) {
            SignalBarGroup(level = info.level, dbm = info.dBm, type = info.type)

            if (info.bands.isNotEmpty()) {
                Text(
                    text = "${resources.getString(R.string.bands_format)}: ${info.bands.joinToString(", ")}",
                    style = TextStyle(
                        fontSize = 12.sp,
                        color = ColorProvider(Color.White)
                    )
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        cancel()
    }
}