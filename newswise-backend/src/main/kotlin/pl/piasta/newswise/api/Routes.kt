package pl.piasta.newswise.api

import org.springframework.http.MediaType.MULTIPART_FORM_DATA
import org.springframework.web.reactive.function.server.coRouter
import pl.piasta.newswise.api.handler.DocumentProcessingHandler
import pl.piasta.newswise.api.handler.PATH_MODEL

fun router(documentProcessingHandler: DocumentProcessingHandler) = coRouter {
    "/processing/{$PATH_MODEL}".nest {
        GET("/remote", documentProcessingHandler::handleRemoteDocumentProcessing)
        accept(MULTIPART_FORM_DATA).nest {
            POST("/files", documentProcessingHandler::handleDocumentFilesProcessing)
        }
    }
}
