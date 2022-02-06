package dev.zwander.cellreader.wear

import android.graphics.Point
import android.util.DisplayMetrics
import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.LocalSize
import androidx.glance.layout.*
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import androidx.glance.wear.tiles.GlanceTileService
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

    @Composable
    override fun Content() {
        val sizePx = Point().apply { wm.defaultDisplay.getSize(this) }
        val sizeDp = sizePx.let { px -> DisplayMetrics().run {
            wm.defaultDisplay.getMetrics(this)

            DpSize((px.x / this.density).dp, (px.y / this.density).dp)
        } }

        val items = CellModelWear.sortedSubIds.map { subId ->
            val cellInfo = CellModelWear.cellInfos[subId]
            val strength = CellModelWear.strengthInfos[subId]

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

    override fun onCreate() {
        super.onCreate()

        launch {
            DataHandler.getInstance(this@CellTile)
                .addHandle(this@CellTile)
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        launch {
            DataHandler.getInstance(this@CellTile)
                .removeHandle(this@CellTile)

            this@CellTile.cancel()
        }
    }
}