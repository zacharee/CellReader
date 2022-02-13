package dev.zwander.cellreader.data

import androidx.compose.runtime.mutableStateMapOf

data class GraphInfo(
    val subId: Int,
    val lines: MutableMap<String, GraphLineInfo> = mutableStateMapOf()
)