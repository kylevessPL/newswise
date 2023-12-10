#!/bin/sh

///bin/true <<EOC
/*
EOC
kotlin -Xplugin="${KOTLIN_HOME}/lib/kotlinx-serialization-compiler-plugin.jar" "$0" "$@"
exit $?
*/

@file:Import("commons.kt")
@file:DependsOn(
    "io.ktor:ktor-client-cio-jvm:2.3.4",
    "io.ktor:ktor-client-logging-jvm:2.3.4",
    "io.ktor:ktor-client-auth-jvm:2.3.4"
)
@file:DependsOn("org.jetbrains.kotlinx:kotlinx-serialization-json-jvm:1.6.0")
@file:CompilerOptions("-jvm-target", "19", "-Xopt-in=kotlin.RequiresOptIn")

import Preprocessing_main.NewsCategoryLabels.BUSINESS_MONEY_LABEL
import Preprocessing_main.NewsCategoryLabels.CRIME_LEGAL_LABEL
import Preprocessing_main.NewsCategoryLabels.ENTERTAINMENT_ARTS_LABEL
import Preprocessing_main.NewsCategoryLabels.LIFESTYLE_LABEL
import Preprocessing_main.NewsCategoryLabels.SCI_TECH_EDUCATION_LABEL
import Preprocessing_main.NewsCategoryLabels.SOCIETY_RELIGION_LABEL
import Preprocessing_main.NewsCategoryLabels.SPORTS_HEALTH_LABEL
import Preprocessing_main.NewsCategoryLabels.TRAVEL_FOOD_LABEL
import Preprocessing_main.NewsCategoryLabels.WORLD_POLITICS_LABEL
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BasicAuthCredentials
import io.ktor.client.plugins.auth.providers.basic
import io.ktor.client.plugins.logging.LogLevel.INFO
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsChannel
import io.ktor.utils.io.jvm.javaio.toInputStream
import java.io.File
import java.io.InputStream
import java.lang.System.err
import java.lang.System.getenv
import java.nio.file.Path
import java.nio.file.Paths
import java.util.zip.ZipInputStream
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

val kaggleUser: String = getenv("KAGGLE_USER") ?: error("KAGGLE_USER environment variable is not set!")
val kaggleKey: String = getenv("KAGGLE_KEY") ?: error("KAGGLE_KEY environment variable is not set!")
val kaggleDatasetUrl = "https://www.kaggle.com/api/v1/datasets/download/rmisra/news-category-dataset"

val maxRetryCount = 5
val requestTimeout = 5000L
val connectTimeout = 3000L

val dataPath: Path = getenv("DATA_PATH")
    ?.let { Path(it).resolve("data.dat") }
    ?: error("DATA_PATH environment variable is not set!")
val fullCleanup: Boolean = getenv("FULL_CLEANUP")?.toBoolean() ?: false
val scrapingScriptPath: Path = Paths.get(".").resolve("scraping.py")

val httpClient = HttpClient {
    expectSuccess = true
    install(Logging) {
        logger = object : Logger {
            override fun log(message: String) = println(message)
        }
        level = INFO
    }
    install(HttpTimeout) {
        requestTimeoutMillis = requestTimeout
        connectTimeoutMillis = connectTimeout
    }
    install(HttpRequestRetry) {
        retryOnExceptionOrServerErrors(maxRetryCount)
        exponentialDelay()
    }
    install(Auth) {
        basic {
            sendWithoutRequest { true }
            credentials {
                BasicAuthCredentials(kaggleUser, kaggleKey)
            }
        }
    }
}

val json = Json {
    ignoreUnknownKeys = true
    coerceInputValues = true
}

val Boolean.intValue
    get() = if (this) 1 else 0

fun printErr(message: String) = err.println(message)

@Suppress("UNRESOLVED_REFERENCE")
fun <T, R> Flow<T>.parallelMap(
    transform: suspend (T) -> R
): Flow<R> = this.concurrentMap(dispatcher = IO, transform = transform)

fun <T> Flow<T>.onError(action: suspend FlowCollector<T>.(cause: Throwable) -> Unit) = catch {
    action(it)
    throw it
}

suspend fun ZipInputStream.first(dispatcher: CoroutineDispatcher = IO) = withContext(dispatcher) {
    runCatching {
        nextEntry!!.let { this@first }
    }.onFailure {
        close()
    }.getOrThrow()
}

suspend fun InputStream.asZip() = withContext(IO) {
    runCatching {
        ZipInputStream(this@asZip)
    }.onFailure {
        close()
    }.getOrThrow()
}

@Suppress("UNRESOLVED_REFERENCE", "RedundantSuspendModifier")
suspend fun Path.createFile(): File = this.newFile()

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
}

enum class NewsCategory(val label: String) {
    @SerialName("U.S. NEWS")
    US_NEWS(WORLD_POLITICS_LABEL),

    @SerialName("WORLD NEWS")
    WORLD_NEWS(WORLD_POLITICS_LABEL),

    @SerialName("THE WORLDPOST")
    THE_WORLD_POST(WORLD_POLITICS_LABEL),

    @SerialName("WORLDPOST")
    WORLD_POST(WORLD_POLITICS_LABEL),

    @SerialName("POLITICS")
    POLITICS(WORLD_POLITICS_LABEL),

    COMEDY(ENTERTAINMENT_ARTS_LABEL),

    @SerialName("CULTURE & ARTS")
    CULTURE_AND_ARTS(ENTERTAINMENT_ARTS_LABEL),
    ENTERTAINMENT(ENTERTAINMENT_ARTS_LABEL),

    @SerialName("ARTS & CULTURE")
    ARTS_AND_CULTURE(ENTERTAINMENT_ARTS_LABEL),
    ARTS(ENTERTAINMENT_ARTS_LABEL),

    PARENTING(LIFESTYLE_LABEL),
    PARENTS(LIFESTYLE_LABEL),

    @SerialName("STYLE & BEAUTY")
    STYLE_AND_BEAUTY(LIFESTYLE_LABEL),
    STYLE(LIFESTYLE_LABEL),

    @SerialName("HOME & LIVING")
    HOME_AND_LIVING(LIFESTYLE_LABEL),
    WOMEN(LIFESTYLE_LABEL),

    SPORTS(SPORTS_HEALTH_LABEL),

    @SerialName("HEALTHY LIVING")
    HEALTHY_LIVING(SPORTS_HEALTH_LABEL),
    WELLNESS(SPORTS_HEALTH_LABEL),

    @SerialName("QUEER VOICES")
    QUEER_VOICES(SOCIETY_RELIGION_LABEL),

    @SerialName("BLACK VOICES")
    BLACK_VOICES(SOCIETY_RELIGION_LABEL),

    @SerialName("LATINO VOICES")
    LATINO_VOICES(SOCIETY_RELIGION_LABEL),
    RELIGION(SOCIETY_RELIGION_LABEL),

    @SerialName("FOOD & DRINK")
    FOOD_AND_DRINK(TRAVEL_FOOD_LABEL),
    TRAVEL(TRAVEL_FOOD_LABEL),
    TASTE(TRAVEL_FOOD_LABEL),

    BUSINESS(BUSINESS_MONEY_LABEL),
    MONEY(BUSINESS_MONEY_LABEL),

    SCIENCE(SCI_TECH_EDUCATION_LABEL),
    ENVIRONMENT(SCI_TECH_EDUCATION_LABEL),
    GREEN(SCI_TECH_EDUCATION_LABEL),
    EDUCATION(SCI_TECH_EDUCATION_LABEL),
    COLLEGE(SCI_TECH_EDUCATION_LABEL),
    TECH(SCI_TECH_EDUCATION_LABEL),

    CRIME(CRIME_LEGAL_LABEL),
    DIVORCE(CRIME_LEGAL_LABEL),
    WEDDINGS(CRIME_LEGAL_LABEL)
}

@Suppress("PLUGIN_IS_NOT_ENABLED")
@Serializable
class NewsItem(@SerialName("link") val url: String, val category: NewsCategory? = null, var content: String? = null) {
    override fun toString() = "${category?.label} $content"
}

suspend fun preprocess() = withContext(IO) {
    dataPath.createFile().bufferedWriter().use { writer ->
        preprocessDataset().collect {
            writer.appendLine(it)
        }
    }
}

@Suppress("UNRESOLVED_REFERENCE")
suspend fun datasetJsonFlow(): Flow<String> = downloadDataset().bufferedReader().useLinesFlow()

@Suppress("UNRESOLVED_REFERENCE", "RedundantSuspendModifier")
suspend fun Array<String>.commandRun(): String = this.runCommand()

suspend fun downloadDataset() = runCatching {
    println("Downloading dataset...")
    httpClient.get(kaggleDatasetUrl)
        .bodyAsChannel()
        .toInputStream()
        .asZip()
        .first()
}.onSuccess {
    println("Dataset downloaded successfully.")
}.onFailure {
    printErr("Error downloading dataset.")
}.getOrThrow()

suspend fun preprocessDataset() = datasetJsonFlow()
    .onError { printErr("Error extracting dataset.") }
    .parallelMap { prepareItem(it) }
    .filterNotNull()
    .onError { printErr("Dataset read error.") }
    .onCompletion { println("Datset processed successfully.") }
    .flowOn(Default)

suspend fun prepareItem(jsonItem: String) = json.decodeFromString<NewsItem>(jsonItem)
    .takeIf { it.category != null }
    ?.apply { content = collectItemContent(url) }
    ?.takeIf { it.content != null }
    ?.toString()

suspend fun collectItemContent(url: String) = withContext(IO) {
    println("Started collecting item content of url: $url")
    val path = scrapingScriptPath.absolutePathString()
    arrayOf("python", path, url, "${fullCleanup.intValue}").commandRun().ifBlank {
        printErr("Item content of url: $url is malformed or empty, ignoring.")
        null
    }?.also { println("Finished collecting item content of url: $url") }
}

runBlocking {
    println("Preprocessing started.")
    runCatching {
        preprocess()
    }.onSuccess {
        println("Preprocessing finished with success.")
    }.onFailure {
        printErr("Preprocessing finished with failure.")
    }.getOrThrow()
}
