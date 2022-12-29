package dev.zwander.cellreader.data.util

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.asExecutor
import java.util.concurrent.Executor
import kotlin.coroutines.ContinuationInterceptor
import kotlin.coroutines.CoroutineContext

fun CoroutineContext.asExecutor(): Executor = (get(ContinuationInterceptor) as CoroutineDispatcher).asExecutor()
