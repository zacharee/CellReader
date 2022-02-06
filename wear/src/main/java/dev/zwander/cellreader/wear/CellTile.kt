package dev.zwander.cellreader.wear

import android.telephony.CellSignalStrength
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.layout.*
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
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

        val itemGridArray by derivedStateOf {
            val grid = hashMapOf<Int, MutableList<TileItemInfo>>()
            val rowSize = 2

            items.forEachIndexed { index, tileItemInfo ->
                val gridRowIndex = index / rowSize
                val gridColumnIndex = index % rowSize

                if (!grid.containsKey(gridRowIndex)) {
                    grid[gridRowIndex] = mutableListOf()
                }

                grid[gridRowIndex]?.add(gridColumnIndex, tileItemInfo)
            }

            grid
        }

        ItemGrid(itemGridArray = itemGridArray)
    }

    @Composable
    private fun ItemGrid(itemGridArray: Map<Int, List<TileItemInfo>>) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically,
            modifier = GlanceModifier.fillMaxSize()
        ) {
            itemGridArray.forEach { (_, columns) ->
                Row(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    columns.forEachIndexed { _, column ->
                        Item(column)
                    }
                }
            }
        }
    }

    @Composable
    private fun Item(info: TileItemInfo) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SignalBarGroup(level = info.level, dbm = info.dBm, type = info.type)

            Log.e("CellReader", "$info")

            if (info.bands.isNotEmpty()) {
                Text(
                    text = "${resources.getString(R.string.bands_format)}: ${info.bands.joinToString(", ")}",
                    style = TextStyle(
                        fontSize = 12.sp,
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