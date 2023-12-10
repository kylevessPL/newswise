@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class, kotlinx.coroutines.FlowPreview::class)

import java.io.Reader
import java.io.File
import java.nio.file.Path
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.DEFAULT_CONCURRENCY
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import kotlinx.coroutines.async

fun Reader.useLinesFlow() = flow {
    useLines {
        emitAll(it.asFlow())
    }
}.flowOn(IO)

fun <T, R> Flow<T>.concurrentMap(
    dispatcher: CoroutineDispatcher = Default,
    concurrency: Int = DEFAULT_CONCURRENCY,
    transform: suspend (T) -> R
) = flatMapMerge(concurrency) {
    flow { emit(transform(it)) }
}.flowOn(dispatcher)

suspend fun Path.newFile(): File = withContext(IO) {
    toFile().also {
        it.parentFile?.mkdirs()
    }
}

suspend fun Array<String>.runCommand(trial: Int = 1): String {
    return withContext(IO) {
        val process = ProcessBuilder(*this@runCommand).start()
        val result1 = async {
            process.errorReader().use { reader ->
                reader.readText()
                    .takeIf { it.isNotEmpty() }
                    ?.let {
                        if (trial < 3) runCommand(trial + 1)
                        else error(it)
                    }
            }
        }
        val result2 = async {
            process.inputReader().use {
                it.readText()
            }
        }
        process.waitFor()
        result1.await() ?: result2.await()
    }
}
