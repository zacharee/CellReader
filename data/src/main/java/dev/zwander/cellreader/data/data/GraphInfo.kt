package dev.zwander.cellreader.data.data

import androidx.compose.runtime.mutableStateMapOf

data class GraphInfo(
    val subId: Int,
    val lines: MutableMap<String, GraphLineInfo> = mutableStateMapOf()
)