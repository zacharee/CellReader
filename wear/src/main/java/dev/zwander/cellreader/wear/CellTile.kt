package dev.zwander.cellreader.wear

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.background
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
    )

    @Composable
    override fun Content() {
        val grid = hashMapOf<Int, MutableList<TileItemInfo>>()
        val rowSize = 2

        val items = arrayListOf<TileItemInfo>()

        CellModelWear.sortedSubIds.forEach { subId ->
            val cellInfo = CellModelWear.cellInfos[subId]
            val strength = CellModelWear.strengthInfos[subId]

            cellInfo?.get(0)?.let {
                items.add(
                    TileItemInfo(
                        it.cellSignalStrength.level,
                        it.cellSignalStrength.dbm,
                        it.cellIdentity.bands,
                        it.cellIdentity.typeString(this@CellTile)
                    )
                )
            }

            strength?.get(0)?.let {
                items.add(
                    TileItemInfo(
                        it.level,
                        it.dbm,
                        listOf(),
                        it.typeString(this@CellTile)
                    )
                )
            }
        }

        items.forEachIndexed { index, tileItemInfo ->
            val gridRowIndex = index / rowSize
            val gridColumnIndex = index % rowSize

            if (!grid.containsKey(gridRowIndex)) {
                grid[gridRowIndex] = mutableListOf()
            }

            grid[gridRowIndex]?.add(gridColumnIndex, tileItemInfo)
        }

        ItemGrid(itemGridArray = grid, GlanceModifier.fillMaxSize())
    }

    @Composable
    private fun ItemGrid(itemGridArray: Map<Int, List<TileItemInfo>>, modifier: GlanceModifier) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier.background(Color.DarkGray)
        ) {
            itemGridArray.forEach { (_, columns) ->
                Row(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = GlanceModifier.defaultWeight()
                ) {
                    columns.forEachIndexed { _, column ->
                        Item(column, GlanceModifier.defaultWeight())
                    }
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