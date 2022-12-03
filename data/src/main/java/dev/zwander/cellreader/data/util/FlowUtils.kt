package dev.zwander.cellreader.data.util

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch

inline fun <reified T> instantCombine(vararg flows: Flow<T>) = channelFlow {
    val array= Array(flows.size) {
        false to (null as T?) // first element stands for "present"
    }

    flows.forEachIndexed { index, flow ->
        launch {
            flow.collect { emittedElement ->
                array[index] = true to emittedElement
                send(array.filter { it.first }.map { it.second })
            }
        }
    }
}

inline fun <reified T> instantCombine(flows: List<Flow<T>>) = channelFlow {
    val array= Array(flows.size) {
        false to (null as T?) // first element stands for "present"
    }

    flows.forEachIndexed { index, flow ->
        launch {
            flow.collect { emittedElement ->
                array[index] = true to emittedElement
                send(array.filter { it.first }.map { it.second })
            }
        }
    }
}
