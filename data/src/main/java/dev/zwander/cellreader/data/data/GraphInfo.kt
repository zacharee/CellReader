package dev.zwander.cellreader.data.data

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf

data class GraphInfo(
    val subId: Int,
    val lines: MutableMap<String, GraphLineInfo> = mutableStateMapOf()
) {
    val sortedLines by derivedStateOf {
        lines.toSortedMap()
    }
}