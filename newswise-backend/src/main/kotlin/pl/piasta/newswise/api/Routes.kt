package pl.piasta.newswise.api

import org.springframework.http.MediaType.TEXT_EVENT_STREAM
import org.springframework.web.reactive.function.server.coRouter
import pl.piasta.newswise.api.handler.DocumentProcessingHandler
import pl.piasta.newswise.api.handler.PATH_MODEL

fun router(documentProcessingHandler: DocumentProcessingHandler) = coRouter {
    "/processing/{$PATH_MODEL}".nest {
        GET("/remote", documentProcessingHandler::handleRemoteDocumentProcessing)
        accept(TEXT_EVENT_STREAM).nest {
            POST("/files", documentProcessingHandler::handleDocumentFilesProcessing)
        }
    }
}
