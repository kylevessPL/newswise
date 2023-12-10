#!/bin/sh

///bin/true <<EOC
/*
EOC
kotlin "$0" "$@"
exit $?
*/

@file:Import("commons.kt")
@file:DependsOn("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
@file:DependsOn("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
@file:CompilerOptions("-jvm-target", "19")

import java.io.File
import java.lang.System.err
import java.lang.System.getenv
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.bufferedReader
import kotlin.io.path.bufferedWriter
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json

val dataPath = getenv("DATA_PATH")?.let { Path(it) } ?: error("DATA_PATH environment variable is not set!")
val fullCleanup: Boolean = getenv("FULL_CLEANUP")?.toBoolean() ?: false

val originalDataPath: Path = dataPath.resolve("data.dat")
val augmentedDataPath: Path = dataPath.resolve("data_aug.dat")
val augmentingScriptPath: Path = Paths.get(".").resolve("augmenting.py")

val Boolean.intValue
    get() = if (this) 1 else 0

fun printErr(message: String) = err.println(message)

@Suppress("UNRESOLVED_REFERENCE")
fun <T, R> Flow<T>.parallelMap(
    concurrency: Int = 8,
    transform: suspend (T) -> R
): Flow<R> = this.concurrentMap(IO, concurrency, transform)

@Suppress("UNRESOLVED_REFERENCE", "RedundantSuspendModifier")
suspend fun Path.createFile(): File = this.newFile()

@Suppress("UNRESOLVED_REFERENCE", "RedundantSuspendModifier")
suspend fun Array<String>.commandRun(): String = this.runCommand()

fun Double.ceil() = kotlin.math.ceil(this).toInt()

infix fun <A, B, C> A.via(pair: Pair<B, C>) = Triple(this, pair.first, pair.second)

fun String.deserializeArray(): Array<String> = Json.decodeFromString(this)

inline fun Array<String>.onEmpty(crossinline action: () -> Unit) = ifEmpty { also { action() } }

@Suppress("UNRESOLVED_REFERENCE")
fun originalDataFlow(): Flow<String> = originalDataPath.bufferedReader().useLinesFlow()

fun augmentedDataFlow(label: String, limit: Int, times: Int) = originalDataFlow()
    .parallelMap { it.split(" ", limit = 2) }
    .filter { it.first() == label }
    .transform { collectAndFlatten(it[0], it[1], times) }
    .take(limit)

suspend fun FlowCollector<String>.collectAndFlatten(label: String, originalContent: String, times: Int) =
    collectAugmentedContent(originalContent, times).forEach {
        emit("$label $it")
    }

suspend fun collectAugmentedContent(originalContent: String, times: Int): Array<String> {
    val path = augmentingScriptPath.absolutePathString()
    return arrayOf("python", path, originalContent, "$times", "${fullCleanup.intValue}")
        .commandRun()
        .deserializeArray()
        .onEmpty { printErr("Some augmented content is malformed or empty, ignoring.") }
}

fun calculateBoundaries(): List<Triple<String, Int, Int>> {
    val labelCounts = originalDataPath.bufferedReader().useLines { lines ->
        lines.map { it.split(" ").first() }
            .groupingBy { it }
            .eachCount()
    }
    val maxCount = labelCounts.values.max()
    return labelCounts
        .filterValues { it != maxCount }
        .map { (label, count) -> label via calculateIntBoundaties(count, maxCount) }
}

fun calculateIntBoundaties(count: Int, maxCount: Int): Pair<Int, Int> {
    val difference = maxCount - count
    val times = difference.toDouble().div(count).ceil()
    return difference to times
}

fun augmentData(boundaries: List<Triple<String, Int, Int>>) = boundaries
    .map { augmentedDataFlow(it.first, it.second, it.third) }
    .toTypedArray()
    .let { merge(*it) }

suspend fun augment() {
    val limits = calculateBoundaries()
    augmentedDataPath.bufferedWriter().use { writer ->
        augmentData(limits).collect {
            writer.appendLine(it)
        }
    }
}

runBlocking(IO) {
    println("Augmenting started.")
    runCatching {
        augment()
    }.onSuccess {
        println("Augmenting finished with success.")
    }.onFailure {
        printErr("Augmenting finished with failure.")
    }.getOrThrow()
}
