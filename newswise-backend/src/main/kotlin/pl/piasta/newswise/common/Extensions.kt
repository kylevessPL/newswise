package pl.piasta.newswise.common

import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation
import edu.stanford.nlp.ling.CoreLabel
import edu.stanford.nlp.pipeline.Annotation
import io.github.pepperkit.corenlp.stopwords.StopWordsAnnotator
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.ObjectInputStream
import java.net.URI
import java.net.URL
import java.nio.file.DirectoryNotEmptyException
import java.nio.file.Files
import java.nio.file.Path
import java.time.Instant
import java.util.Properties
import kotlin.io.path.Path
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.DEFAULT_CONCURRENCY
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.apache.commons.compress.archivers.sevenz.SevenZFile
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.exception.ExceptionUtils
import org.apache.tika.Tika
import org.apache.tika.io.TikaInputStream
import org.apache.tika.metadata.Metadata
import org.apache.tika.parser.ParseContext
import org.apache.tika.parser.Parser
import org.springframework.core.env.MutablePropertySources
import org.springframework.core.env.PropertiesPropertySource
import org.springframework.core.env.PropertySourcesPropertyResolver
import org.springframework.core.io.ResourceLoader
import org.springframework.http.codec.ServerSentEvent
import org.springframework.http.codec.multipart.FilePart
import org.springframework.http.codec.multipart.Part
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.bodyToFlow
import org.xml.sax.ContentHandler
import pl.piasta.newswise.api.validate
import pl.piasta.newswise.configuration.ReactiveFilePart
import pl.piasta.newswise.configuration.ServerSentEventsWrapper

/**
 * Remove non-letter characters from the string.
 *
 * @return A new string containing only the letters from the original string.
 */
fun String.removeNonLetters() = filter { it.isLetter() }

/**
 * Remove repeated characters in the string.
 *
 * @return A new string with repeated characters removed.
 */
fun String.removeRepeatedCharacters() = "(.)\\1+".toRegex().replace(this) {
    it.groupValues[1] + it.groupValues[1]
}

/**
 * Remove single-character words from the string.
 *
 * @return A new string with single-character words removed.
 */
fun String.removeSingleCharacterWords() = split("\\s".toRegex())
    .filter { it.length > 1 }
    .joinToString(" ")

/**
 * Remove all whitespaces from the string.
 *
 * @return A new string with all whitespaces removed.
 */
fun String.removeAllWhitespaces(): String = StringUtils.deleteWhitespace(this)

/**
 * Normalize the string by stripping accents.
 *
 * @return A new string with accents stripped.
 */
fun String.normalize(): String = StringUtils.stripAccents(this)

/**
 * Remove URLs from the string.
 *
 * @return A new string with URLs removed.
 */
@Suppress("RegExpSimplifiable")
fun String.removeUrls(): String {
    val urlFree = split("\\s".toRegex())
        .filterNot { StringUtils.startsWithAny(it, "www", "http") || it.endsWith(".html") }
        .joinToString(" ")
    return "http[\\w]*://[\\w]*\\.?[\\w-]+\\.+[\\w]+/[\\w]+".toRegex()
        .find(urlFree)
        ?.let { urlFree.replace(it.value.trim(), "") }
        ?: urlFree
}

/**
 * Remove words containing numeric characters from the string.
 *
 * @return A new string with words containing numeric characters removed.
 */
fun String.removeAlphanumerics() = split(" ")
    .filter { !it.any { ch -> ch.isDigit() } }
    .joinToString(" ")

/**
 * Check if the current string, ignoring case, is equal to another string.
 *
 * @param other The string to compare with.
 * @return `true` if the strings are equal (ignoring case), `false` otherwise.
 */
fun String?.equalsIgnoreCase(other: String?) = equals(other, true)

/**
 * Resolve placeholders in the string using the provided Properties.
 *
 * @param properties The Properties containing the values for the placeholders.
 * @return A new string with placeholders resolved using the specified Properties.
 */
fun String.resolvePlaceholders(properties: Properties) = MutablePropertySources()
    .apply { addFirst(PropertiesPropertySource("properties", properties)) }
    .let { PropertySourcesPropertyResolver(it).resolvePlaceholders(this) }

/**
 * Convert the string representation to a [Path] object.
 *
 * @return A [Path] object representing the string.
 */
fun String.toPath() = Path(this)

/**
 * Convert the string representation to a [URI].
 *
 * @return A [URI] object representing the string.
 */
fun String.toUri() = URI(this)

/**
 * Convert a string value to an enum constant of the specified enum type.
 *
 * @param value The string representation of the enum constant.
 * @param uppercase Whether to treat the enum constant names case-insensitively by converting to uppercase.
 *                  Default is `true`.
 * @return The enum constant of the specified enum type.
 * @throws IllegalArgumentException if the specified enum type has no constant with the specified name,
 *         or the specified class object does not represent an enum type.
 */
inline fun <reified E : Enum<E>> fromString(value: String, uppercase: Boolean = true): E {
    val name = if (uppercase) value.uppercase() else value
    return enumValueOf(name)
}

/**
 * Create an [ObjectInputStream] from the [InputStream].
 *
 * @return An [ObjectInputStream] created from the [InputStream].
 */
fun InputStream.asObjectInputStream() = ObjectInputStream(this)

/**
 * Extract an [InputStream] of a first entry of a 7-Zip archive file.
 *
 * @return An [InputStream] representing the contents of the first entry in the 7-Zip archive,
 *         or null if the archive is empty or cannot be read.
 */
fun File.sevenZipSingleStream(): InputStream? {
    val zipFile = SevenZFile(this)
    return zipFile.nextEntry.let { zipFile.getInputStream(it) }
}

/**
 * Create a string representation of the map by joining key-value pairs with the specified separator.
 *
 * @param separator The character used to separate key-value pairs in the resulting string. Default is ','.
 * @return A string representation of the map with key-value pairs joined using the specified separator.
 */
fun Map<String, Any>.joinToString(separator: Char = ',') = mapValues { (_, value) -> value.toString() }
    .map { "${it.key}=${it.value}" }
    .joinToString("$separator")

/**
 * Convert the values of the map to their string representations and create a [Properties] object.
 *
 * @return A [Properties] object with string representations of the original map values.
 */
fun Map<String, Any>.toStringProperties() = mapValues { (_, value) -> value.toString() }.toProperties()

/**
 * Returns the first value in the [Map] matching the given predicate, or null if no such value is found.
 *
 * @param predicate The predicate function to test keys.
 * @return The first value matching the predicate, or null if no such value is found.
 */
fun <K, V> Map<K, V>.firstOrNull(predicate: (K) -> Boolean) = filterKeys(predicate).values.firstOrNull()

/**
 * Convert the string representation to an Instant or return null if the conversion fails.
 *
 * @return The Instant parsed from the string, or null if the conversion fails.
 */
fun String.toInstantOrNull(): Instant? {
    val date = if (endsWith('Z')) this else "${this}Z"
    return runCatching { Instant.parse(date) }.getOrNull()
}

/**
 * Get the root cause of a [Throwable].
 */
val Throwable.rootCause: Throwable
    get() = ExceptionUtils.getRootCause(this) ?: this

/**
 * Validate the current [ServerRequest]. If validation succeeds,
 * invoke the provided handler function to generate a [ServerResponse].
 * It is a suspended alternative to the request method.
 *
 * @param handler A suspend lambda function representing the handler logic, with the current [ServerRequest]
 *   as its receiver, to generate a [ServerResponse] upon successful validation.
 * @return The resulting [ServerResponse] from the execution of the handler function.
 */
suspend inline fun <reified T : ServerRequest> T.validateAndAwait(noinline handler: suspend T.() -> ServerResponse) =
    validate.requestAndAwait(this) {
        handler()
    }

/**
 * Deletes the file if it exists.
 *
 * @param recursive If `true`, and if the file is a directory, deletes the directory and its contents recursively.
 * @param dispatcher The coroutine dispatcher to use for the deletion operation. Defaults to [Dispatchers.IO].
 * @return `true` if the file was deleted, `false` if the file does not exist or deletion failed.
 *
 * @throws [DirectoryNotEmptyException] if the file is a directory and could not otherwise be deleted
 *         because the directory is not empty (optional specific exception).
 * @throws [IOException] if an I/O error occurs.
 * @throws [SecurityException] In the case of the default provider, and a security manager is
 *   installed, the [checkDelete][SecurityManager.checkDelete] method is invoked to check delete access to the file.
 */
@Suppress("removal")
suspend fun File.deleteIfExists(recursive: Boolean = false, dispatcher: CoroutineDispatcher = Dispatchers.IO) =
    withContext(dispatcher) {
        if (recursive) deleteRecursively()
        else Files.deleteIfExists(toPath())
    }

/**
 * Retrieves a flow of [file parts][FilePart] from the request's body as [ReactiveFilePart] instances.
 *
 * @return A flow of [ReactiveFilePart] instances extracted from the request's body.
 */
fun ServerRequest.filePartFlow() = bodyToFlow<Part>()
    .filterIsInstance<FilePart>()
    .map { ReactiveFilePart(it) }

/**
 * Transform the current [flow][Flow] of elements into a [flow][Flow] of [ServerSentEvent].
 *
 * @return A [flow][Flow] emitting [ServerSentEvent] elements.
 */
fun <T> Flow<T>.asServerSentEvents() = ServerSentEvent.builder<T>().run {
    val startEvent = event("start").build()
    val endEvent = event("end").build()
    ServerSentEventsWrapper(startEvent, endEvent).wrap(this@asServerSentEvents)
}

/**
 * Apply the provided suspending transformation function to each element of the flow concurrently,
 * emitting the transformed values as a new flow.
 *
 * @param dispatcher The [dispatcher][CoroutineDispatcher] to be used for concurrent execution.
 *   By default, it is equal to [Default][Dispatchers.Default].
 * @param concurrencyLevel The maximum number of concurrent transformations to perform.
 *   By default, it is equal to [DEFAULT_CONCURRENCY].
 * @param transform The transformation function to be applied to each element.
 * @return A new Flow emitting the transformed values concurrently.
 */
fun <T, R> Flow<T>.concurrentMap(
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
    concurrencyLevel: Int = DEFAULT_CONCURRENCY,
    transform: suspend (T) -> R
) = flatMapMerge(concurrencyLevel) {
    flow { emit(transform(it)) }
}.flowOn(dispatcher)

/**
 * Get the content type of the file.
 *
 * @return The content type of the file.
 */
val File.contentType: String
    get() = Tika().detect(this)

/**
 * Parse the content from the specified URL using the parser registered in the ParseContext,
 * along with the specified content handler and metadata.
 *
 * @param url      The URL pointing to the content to be parsed.
 * @param handler  The content handler to process the parsed content.
 * @param metadata The metadata to store information about the parsed content.
 * @param dispatcher The coroutine dispatcher to use for the deletion operation. Defaults to [Dispatchers.IO].
 */
suspend fun ParseContext.parse(
    url: URL,
    handler: ContentHandler,
    metadata: Metadata,
    dispatcher: CoroutineDispatcher = Dispatchers.IO
) = parse(TikaInputStream.get(url, metadata), handler, metadata, dispatcher)

/**
 * Parse the content from the specified input stream using the parser registered in the ParseContext,
 * along with the specified content handler and metadata.
 *
 * @param stream   The input stream containing the content to be parsed.
 * @param handler  The content handler to process the parsed content.
 * @param metadata The metadata to store information about the parsed content. Default is an empty Metadata.
 * @param dispatcher The coroutine dispatcher to use for the deletion operation. Defaults to [Dispatchers.IO].
 */
suspend fun ParseContext.parse(
    stream: InputStream,
    handler: ContentHandler,
    metadata: Metadata = Metadata(),
    dispatcher: CoroutineDispatcher = Dispatchers.IO
) = withContext(dispatcher) {
    stream.use {
        this@parse[Parser::class.java].parse(it, handler, metadata, this@parse)
    }
}

/**
 * Extract the list of CoreLabel tokens from the CoreNLP [Annotation].
 *
 * @return A list of [CoreLabel] tokens.
 */
fun Annotation.tokens(): List<CoreLabel> = this[TokensAnnotation::class.java]

/**
 * Check if the CoreLabel is annotated as a stop word by the StopWordsAnnotator.
 *
 * @return `true` if the CoreLabel is a stop word, `false` otherwise.
 */
fun CoreLabel.isStopWord(): Boolean = this[StopWordsAnnotator::class.java]

/**
 * Get the absolute path of a resource.
 *
 * @param resourcePath The path to the resource.
 * @return The absolute path string of the resource or null if the resource is not found.
 */
fun ResourceLoader.absolutePathString(resourcePath: String?) = resourcePath?.let {
    getResource(it)
}?.file?.absolutePath

/**
 * Get the [File] of a resource.
 *
 * @param resourcePath The path to the resource.
 * @return The [File] object of the resource or null if the resource is not found.
 */
fun ResourceLoader.file(resourcePath: String?) = resourcePath?.let {
    getResource(it)
}?.file

/**
 * Get the [InputStreamReader] of a resource.
 *
 * @param resourcePath The path to the resource.
 * @return The [InputStreamReader] object of the resource or null if the resource is not found.
 */
fun ResourceLoader.reader(resourcePath: String?) = resourcePath?.let {
    getResource(it)
}?.inputStream?.reader()

/**
 * Get the [URL] of a resource.
 *
 * @param resourcePath The path to the resource.
 * @return The [URL] object of the resource or null if the resource is not found.
 */
fun ResourceLoader.url(resourcePath: String?) = resourcePath?.let {
    getResource(it)
}?.url
