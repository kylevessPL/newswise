package pl.piasta.newswise.configuration

import java.util.UUID
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.withContext
import org.springframework.http.codec.multipart.FilePart
import pl.piasta.newswise.common.createTempFile

class ReactiveFilePart(part: FilePart, private val context: CoroutineDispatcher = Dispatchers.IO) : FilePart by part {
    suspend fun transferToTempDirectory(subdirectory: String = UUID.randomUUID().toString()) = coroutineScope {
        createTempFile(filename(), subdirectory, context).also {
            withContext(context) {
                transferTo(it).awaitSingleOrNull()
                delete().awaitFirstOrNull()
            }
        }
    }
}
