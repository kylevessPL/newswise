package pl.piasta.newswise.api.handler

import java.io.File
import java.net.URL
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.map
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse.badRequest
import org.springframework.web.reactive.function.server.ServerResponse.ok
import org.springframework.web.reactive.function.server.bodyAndAwait
import org.springframework.web.reactive.function.server.bodyValueAndAwait
import org.springframework.web.reactive.function.server.json
import org.springframework.web.reactive.function.server.queryParamOrNull
import org.springframework.web.reactive.function.server.sse
import org.springframework.web.server.ServerWebInputException
import pl.piasta.newswise.common.asServerSentEvents
import pl.piasta.newswise.common.concurrentMap
import pl.piasta.newswise.common.deleteIfExists
import pl.piasta.newswise.common.filePartFlow
import pl.piasta.newswise.common.toUri
import pl.piasta.newswise.common.validateAndAwait
import pl.piasta.newswise.processing.CategorizerModel
import pl.piasta.newswise.processing.DocumentProcessingException
import pl.piasta.newswise.processing.DocumentProcessor

const val PATH_MODEL = "model"
private const val QUERY_URL = "url"
private const val PROCESSING_FILES_PATH = "classification"

class DocumentProcessingHandler(private val documentProcessor: DocumentProcessor) {
    private val ServerRequest.model: CategorizerModel
        get() = CategorizerModel.fromModel(pathVariable(PATH_MODEL))!!

    suspend fun handleDocumentFilesProcessing(request: ServerRequest) = request.validateAndAwait {
        val events = filePartFlow()
            .map { it.name() to it.transferToTempDirectory(PROCESSING_FILES_PATH) }
            .concurrentMap { model.process(it.first, it.second) }
            .asServerSentEvents()
        ok().sse().bodyAndAwait(events)
    }

    suspend fun handleRemoteDocumentProcessing(request: ServerRequest) = request.validateAndAwait {
        val url = request.queryParamOrNull(QUERY_URL).validateUrlAndGet()
        val result = coroutineScope {
            async { model.process(url) }
        }
        try {
            ok().json().bodyValueAndAwait(result.await())
        } catch (ex: DocumentProcessingException) {
            badRequest().json().bodyValueAndAwait(ex.message!!)
        }
    }

    private fun String?.validateUrlAndGet() = try {
        this!!.toUri().toURL()
    } catch (ex: Exception) {
        throw ServerWebInputException("Parameter $QUERY_URL is not valid")
    }

    private suspend fun CategorizerModel.process(name: String, file: File) = try {
        documentProcessor.process(name, file, this)
    } finally {
        file.deleteIfExists()
    }

    private suspend fun CategorizerModel.process(url: URL) = documentProcessor.process(url, this)
}
