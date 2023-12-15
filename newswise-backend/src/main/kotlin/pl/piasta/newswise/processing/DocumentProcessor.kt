package pl.piasta.newswise.processing

import java.io.File
import java.net.URL
import kotlinx.coroutines.coroutineScope
import pl.piasta.newswise.common.contentType
import pl.piasta.newswise.common.log
import pl.piasta.newswise.extraction.DocumentExtractor
import pl.piasta.newswise.processing.DocumentProcessingException.UnsupportedDocumentTypeException

interface DocumentProcessor {
    suspend fun process(url: URL, categorizer: CategorizerModel): RemoteDocumentProcessingResultDto
    suspend fun process(name: String, file: File, categorizer: CategorizerModel): FileDocumentProcessingResultDto
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
        val (metadata, category) = url.processDocument(categorizer)
        return RemoteDocumentProcessingResultDto(metadata, category)
    }

    override suspend fun process(name: String, file: File, categorizer: CategorizerModel) = runCatching {
        if (file.contentType !in processingProperties.contentTypes) throw UnsupportedDocumentTypeException()
        val (metadata, category) = file.toURI().toURL().processDocument(categorizer)
        FileDocumentProcessingResultDto.Success(name, metadata, category)
    }.getOrElse {
        when (it) {
            is DocumentProcessingException -> FileDocumentProcessingResultDto.Failure(name, it)
            else -> throw it
        }
    }

    private suspend fun URL.processDocument(categorizer: CategorizerModel) = coroutineScope {
        runCatching {
            val document = documentExtractor.extract(this@processDocument)
            val category = documentCategorizationManager.categorize(document.content, categorizer)
            document.metadata to category
        }.getOrElse {
            logger.warn("Document processing error", it)
            throw it
        }
    }
}
