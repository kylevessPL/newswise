package pl.piasta.newswise.extraction

import com.github.pemistahl.lingua.api.LanguageDetector
import de.l3s.boilerpipe.extractors.ArticleSentencesExtractor
import java.net.URL
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.tika.metadata.Metadata
import org.apache.tika.parser.ParseContext
import org.apache.tika.sax.BodyContentHandler
import org.apache.tika.sax.boilerpipe.BoilerpipeContentHandler
import org.languagetool.Language
import org.languagetool.MultiThreadedJLanguageTool
import org.languagetool.tools.Tools
import pl.piasta.newswise.common.parse
import pl.piasta.newswise.processing.DocumentProcessingException.InvalidDocumentURLException
import pl.piasta.newswise.processing.DocumentProcessingException.InvalidNewsArticleDocumentException
import pl.piasta.newswise.processing.DocumentProcessingException.UnsupportedDocumentLanguageException

private const val NO_BUFFER_LIMIT = -1

data class ExtractedDocument(val metadata: Map<String, Any>, val content: String)

interface DocumentExtractor {
    suspend fun extract(url: URL): ExtractedDocument
}

class TikaDocumentExtractor(
    private val metadataExtractor: MetadataExtractor,
    private val languageDetector: LanguageDetector,
    private val parser: ParseContext,
    private val hunspellLanguage: Language,
    private val extractionProperties: ExtractionProperties,
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : DocumentExtractor {
    override suspend fun extract(url: URL) = coroutineScope {
        val (metadata, content) = url.process()
        launch { content.validate() }
        val keyMetadata = async { metadata.extractKeyMetadata() }
        val correctedContent = async { content.correctSpelling() }
        ExtractedDocument(keyMetadata.await(), correctedContent.await())
    }

    private suspend fun String.validate() {
        val language = withContext(ioDispatcher) {
            async { detectLanguage() }
        }
        if (isBlank()) throw InvalidNewsArticleDocumentException()
        if (language.await() !in extractionProperties.languages) throw UnsupportedDocumentLanguageException()
    }

    private suspend fun URL.process(): Pair<Metadata, String> {
        val contentHandler = BodyContentHandler(NO_BUFFER_LIMIT)
        val handler = BoilerpipeContentHandler(contentHandler, ArticleSentencesExtractor.INSTANCE)
        val metadata = Metadata()
        runCatching {
            parser.parse(this@process, handler, metadata, ioDispatcher)
        }.getOrElse {
            throw InvalidDocumentURLException()
        }
        return metadata to contentHandler.toString()
    }

    private suspend fun String.detectLanguage() = withContext(defaultDispatcher) {
        languageDetector.detectLanguageOf(this@detectLanguage)
            .isoCode639_3
            .name
            .lowercase()
    }

    private fun Metadata.extractKeyMetadata() = metadataExtractor.extract(this@extractKeyMetadata)

    private suspend fun String.correctSpelling() = withContext(defaultDispatcher) {
        MultiThreadedJLanguageTool(hunspellLanguage).run {
            Tools.correctText(this@correctSpelling, this).also { shutdown() }
        }
    }
}
