package pl.piasta.newswise.processing

import java.io.File
import java.net.URL
import kotlinx.coroutines.coroutineScope
import pl.piasta.newswise.common.contentType
import pl.piasta.newswise.common.log
import pl.piasta.newswise.extraction.DocumentExtractor
import pl.piasta.newswise.extraction.ExtractedDocument
import pl.piasta.newswise.processing.DocumentProcessingException.UnsupportedDocumentTypeException

interface DocumentProcessor {
    suspend fun process(url: URL, categorizer: CategorizerModel): RemoteDocumentProcessingResultDto
    suspend fun process(file: File, categorizer: CategorizerModel): FileDocumentProcessingResultDto
}

class DocumentProcessingService(
    private val documentExtractor: DocumentExtractor,
    private val documentCategorizationManager: DocumentCategorizationManager,
    private val processingProperties: ProcessingProperties
) : DocumentProcessor {
    private companion object {
        val logger by log()
    }

    override suspend fun process(url: URL, categorizer: CategorizerModel): RemoteDocumentProcessingResultDto {
        val (document, category) = url.processDocument(categorizer)
        return RemoteDocumentProcessingResultDto(document, category)
    }

    override suspend fun process(file: File, categorizer: CategorizerModel) = runCatching {
        if (file.contentType !in processingProperties.contentTypes) throw UnsupportedDocumentTypeException()
        val (document, category) = file.toURI().toURL().processDocument(categorizer)
        FileDocumentProcessingResultDto.Success(file, document, category)
    }.getOrElse {
        when (it) {
            is DocumentProcessingException -> FileDocumentProcessingResultDto.Failure(file, it)
            else -> throw it
        }
    }

    private suspend fun URL.processDocument(categorizer: CategorizerModel) = coroutineScope {
        runCatching {
            val document = documentExtractor.extract(this@processDocument)
            val category = categorizer.categorizeDocument(document)
            document to category
        }.getOrElse {
            logger.warn("Document processing error", it)
            throw it
        }
    }

    private suspend fun CategorizerModel.categorizeDocument(document: ExtractedDocument) = documentCategorizationManager
        .categorize(document.content, this)
        .mapKeys { it.key.category }
}
