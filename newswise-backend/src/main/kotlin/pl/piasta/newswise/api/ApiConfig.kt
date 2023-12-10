package pl.piasta.newswise.api

import org.springframework.context.support.beans
import pl.piasta.newswise.api.handler.ApiExceptionHandler
import pl.piasta.newswise.api.handler.DocumentProcessingHandler

val apiConfig = beans {
    bean(::router)
    bean<ApiExceptionHandler>()
    bean<DocumentProcessingHandler>()
}
