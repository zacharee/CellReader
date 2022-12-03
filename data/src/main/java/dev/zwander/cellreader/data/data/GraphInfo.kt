package dev.zwander.cellreader.data.data

import kotlinx.coroutines.flow.MutableStateFlow
import java.util.TreeMap

data class GraphInfo(
    val subId: Int,
    val lines: MutableStateFlow<MutableMap<String, GraphLineInfo>> = MutableStateFlow(TreeMap())
)