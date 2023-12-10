package pl.piasta.newswise.extraction

import java.time.Instant
import kotlin.reflect.KClass
import kotlin.reflect.safeCast
import org.apache.tika.metadata.Metadata
import pl.piasta.newswise.common.mapNotBlank
import pl.piasta.newswise.common.toInstantOrNull

fun interface MetadataExtractor {
    fun extract(metadata: Metadata): Map<String, Any>
}

class DocumentMetadataExtractor : MetadataExtractor {
    override fun extract(metadata: Metadata): Map<String, Any> = DocumentMetadata.entries
        .associate { it.name to it.extractEntry(metadata) }
        .filterValues { it != null }
        .mapValues { it.value as Any }

    private fun DocumentMetadata.extractEntry(metadata: Metadata) = property
        .mapNotBlank { metadata[it] }
        .firstOrNull()
        ?.castTo(type)

    private fun <T : Any> String?.castTo(clazz: KClass<T>): T? {
        val obj: Any? = when (clazz.java) {
            Int::class.java -> this?.toIntOrNull()
            Double::class.java -> this?.toDoubleOrNull()
            Instant::class.java -> this?.toInstantOrNull()
            Enum::class.java -> this?.toIntOrNull()?.let { clazz.java.enumConstants[it] }
            else -> this
        }
        return clazz.safeCast(obj)
    }
}
