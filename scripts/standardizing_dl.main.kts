#!/bin/sh

///bin/true <<EOC
/*
EOC
kotlin "$0" "$@"
exit $?
*/

@file:Import("commons.kt")
@file:DependsOn("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
@file:CompilerOptions("-jvm-target", "19")

import java.io.File
import java.lang.System.err
import java.lang.System.getenv
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.bufferedReader
import kotlin.io.path.bufferedWriter
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.runBlocking

val dataPath = getenv("DATA_PATH")?.let { Path(it) } ?: error("DATA_PATH environment variable is not set!")

val originalDataPath: Path = dataPath.resolve("data_opennlp.dat")
val standarizedDataPath: Path = dataPath.resolve("data_dl.csv")

val headers = arrayOf("text", "label")

fun printErr(message: String) = err.println(message)

@Suppress("UNRESOLVED_REFERENCE")
fun <T, R> Flow<T>.parallelMap(transform: suspend (T) -> R): Flow<R> = this.concurrentMap(IO, transform = transform)

@Suppress("UNRESOLVED_REFERENCE", "RedundantSuspendModifier")
suspend fun Path.createFile(): File = this.newFile()

@Suppress("UNRESOLVED_REFERENCE")
fun originalDataFlow(): Flow<String> = originalDataPath.bufferedReader().useLinesFlow()

fun String.halveAndJoinReversed(
    separator: CharSequence
) = split(" ", limit = 2).reversed().joinToString(separator)

fun standarizedDataFlow() = originalDataFlow()
    .parallelMap { it.halveAndJoinReversed(",") }
    .onStart { emit(headers.joinToString(",")) }

suspend fun standarize() = standarizedDataPath.bufferedWriter().use { writer ->
    standarizedDataFlow().collect {
        writer.appendLine(it)
    }
}

runBlocking(IO) {
    println("Standardizing for DL started.")
    runCatching {
        standarize()
    }.onSuccess {
        println("Standardizing finished with success.")
    }.onFailure {
        printErr("Standardizing finished with failure.")
    }.getOrThrow()
}
