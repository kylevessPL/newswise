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
import kotlin.reflect.full.memberProperties
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flattenConcat
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

val dataPath = getenv("DATA_PATH")?.let { Path(it) } ?: error("DATA_PATH environment variable is not set!")

val originalDataPath: Path = dataPath.resolve("data_opennlp.dat")
val standarizedDataPath: Path = dataPath.resolve("data_weka.arff")

val dataName = "news-articles"

object NewsCategoryLabels {
    const val WORLD_POLITICS_LABEL = "World&Politics"
    const val ENTERTAINMENT_ARTS_LABEL = "Entertainment&Arts"
    const val LIFESTYLE_LABEL = "Lifestyle"
    const val SPORTS_HEALTH_LABEL = "Sports&Health"
    const val SOCIETY_RELIGION_LABEL = "Society&Religion"
    const val TRAVEL_FOOD_LABEL = "Travel&Food"
    const val BUSINESS_MONEY_LABEL = "Business&Money"
    const val SCI_TECH_EDUCATION_LABEL = "SciTech&Education"
    const val CRIME_LEGAL_LABEL = "Crime&Legal"

    val values by lazy {
        this::class.memberProperties
            .filter { it.isConst }
            .map { it.getter.call() as String }
            .toMutableList()
    }
}

fun printErr(message: String) = err.println(message)

val String.Companion.empty
    get() = ""

@OptIn(ExperimentalCoroutinesApi::class)
fun <T> concat(first: Flow<T>, second: Flow<T>) = flowOf(first, second).flattenConcat()

@Suppress("UNRESOLVED_REFERENCE")
fun <T, R> Flow<T>.parallelMap(transform: suspend (T) -> R): Flow<R> = this.concurrentMap(IO, transform = transform)

@Suppress("UNRESOLVED_REFERENCE", "RedundantSuspendModifier")
suspend fun Path.createFile(): File = this.newFile()

@Suppress("UNRESOLVED_REFERENCE")
fun originalDataFlow(): Flow<String> = originalDataPath.bufferedReader().useLinesFlow()

fun String.halveAndJoin(
    separator: CharSequence,
    transform: (String) -> String = { it }
) = split(" ", limit = 2).joinToString(separator, transform = transform)

fun standarizedHeaderFlow() = flowOf(
    "@relation $dataName",
    String.empty,
    NewsCategoryLabels.values.joinToString(",", "@attribute class-att {", "}"),
    "@attribute text-att string",
    String.empty
).flowOn(Default)

fun standarizedDataFlow() = originalDataFlow().parallelMap { line ->
    line.halveAndJoin(",") { "'$it'" }
}.onStart { emit("@data") }.flowOn(IO)

suspend fun standarize() = coroutineScope {
    withContext(IO) {
        standarizedDataPath.bufferedWriter().use { writer ->
            concat(standarizedHeaderFlow(), standarizedDataFlow()).collect {
                writer.appendLine(it)
            }
        }
    }
}

runBlocking {
    println("Standardizing for Weka started.")
    runCatching {
        standarize()
    }.onSuccess {
        println("Standardizing finished with success.")
    }.onFailure {
        printErr("Standardizing finished with failure.")
    }.getOrThrow()
}
