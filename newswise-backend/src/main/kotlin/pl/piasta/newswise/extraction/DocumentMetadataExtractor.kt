package pl.piasta.newswise.extraction

import java.time.Instant
import kotlin.reflect.KClass
import kotlin.reflect.safeCast
import org.apache.tika.metadata.Metadata
import pl.piasta.newswise.common.toEnumOrNull
import pl.piasta.newswise.common.toInstantOrNull

private const val DATETIME_FORMAT_FALLBACK = "EEE MMM dd HH:mm:ss XXX yyyy"

fun interface MetadataExtractor {
    fun extract(metadata: Metadata): Map<String, Any>
}

class DocumentMetadataExtractor : MetadataExtractor {
    override fun extract(metadata: Metadata) = DocumentMetadata.entries
        .associate { it.name to it.extractEntry(metadata) }
        .filterValues { it != null }
        .mapValues { it.value as Any }

    private fun DocumentMetadata.extractEntry(metadata: Metadata) = property
        .mapNotNull { metadata.getValues(it) }
        .flatMap { it.asIterable() }
        .filterNot { it.isBlank() }
        .firstOrNull()
        ?.map(type, customMapper)

    private fun <T : Any> String.map(clazz: KClass<T>, customMapper: ((String) -> Any?)?) = customMapper?.let {
        return@map clazz.safeCast(it(this))
    } ?: castTo(clazz)

    private fun <T : Any> String?.castTo(clazz: KClass<T>): T? {
        val obj: Any? = when (val javaClazz = clazz.java) {
            Int::class.java -> this?.toIntOrNull()
            Double::class.java -> this?.toDoubleOrNull()
            Boolean::class.java -> this?.toBooleanStrictOrNull()
            Instant::class.java -> this?.toInstantOrNull(DATETIME_FORMAT_FALLBACK)
            else -> if (javaClazz.isEnum) this?.toEnumOrNull(clazz) else this
        }
        return clazz.safeCast(obj)
    }
}
