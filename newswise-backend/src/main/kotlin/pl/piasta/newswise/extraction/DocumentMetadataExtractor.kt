package pl.piasta.newswise.extraction

import java.time.Instant
import kotlin.reflect.KClass
import kotlin.reflect.safeCast
import org.apache.tika.metadata.Metadata
import pl.piasta.newswise.common.toInstantOrNull

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
        ?.castTo(type)

    private fun <T : Any> String?.castTo(clazz: KClass<T>): T? {
        val obj: Any? = when (clazz.java) {
            Int::class.java -> this?.toIntOrNull()
            Double::class.java -> this?.toDoubleOrNull()
            Boolean::class.java -> this?.toBooleanStrictOrNull()
            Instant::class.java -> this?.toInstantOrNull()
            Enum::class.java -> this?.toIntOrNull()?.let { clazz.java.enumConstants[it] }
            else -> this
        }
        return clazz.safeCast(obj)
    }
}
