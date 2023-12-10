package pl.piasta.newswise.configuration

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.springframework.http.codec.ServerSentEvent

class ServerSentEventsWrapper<T>(
    private val startEvent: ServerSentEvent<T>? = null,
    private val endEvent: ServerSentEvent<T>? = null
) {
    fun wrap(flow: Flow<T>) = flow {
        startEvent?.let { emit(it) }
        flow.collect { emit(ServerSentEvent.builder<T>().data(it).build()) }
        endEvent?.let { emit(it) }
    }
}
