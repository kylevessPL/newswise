package pl.piasta.newswise.api.handler

import kotlin.reflect.KClass
import org.springframework.core.Ordered
import org.springframework.core.Ordered.HIGHEST_PRECEDENCE
import org.springframework.core.io.buffer.DataBufferLimitException
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebExceptionHandler
import pl.piasta.newswise.common.firstOrNull
import pl.piasta.newswise.common.rootCause
import pl.piasta.newswise.processing.DocumentProcessingException
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

private val apiExceptions: Map<KClass<out Throwable>, HttpStatus> = mapOf(
    DataBufferLimitException::class to BAD_REQUEST,
    DocumentProcessingException::class to BAD_REQUEST
)

/**
 * [WebExceptionHandler] that handles specific API exceptions and wraps them into
 * [ResponseStatusException] instances, to be further processed by underlying handlers.
 */
class ApiExceptionHandler : WebExceptionHandler, Ordered {
    override fun handle(exchange: ServerWebExchange, ex: Throwable): Mono<Void> = apiExceptions
        .firstOrNull { it.isInstance(ex) }
        ?.let { ResponseStatusException(it, ex.rootCause.message, ex).toMono() }
        ?: ex.toMono()

    override fun getOrder() = HIGHEST_PRECEDENCE
}
