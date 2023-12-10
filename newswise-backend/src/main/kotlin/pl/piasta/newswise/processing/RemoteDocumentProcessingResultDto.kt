package pl.piasta.newswise.processing

import java.io.File
import pl.piasta.newswise.common.rootCause
import pl.piasta.newswise.extraction.ExtractedDocument

private interface DocumentProcessingResult {
    val metadata: Map<String, Any>
    val predictions: Map<String, Int>
}

data class RemoteDocumentProcessingResultDto(
    override val metadata: Map<String, Any>,
    override val predictions: Map<String, Int>
) : DocumentProcessingResult {
    constructor(document: ExtractedDocument, predictions: Map<String, Int>) : this(document.metadata, predictions)
}

sealed class FileDocumentProcessingResultDto {
    abstract val filename: String

    data class Success(
        override val filename: String,
        override val metadata: Map<String, Any>,
        override val predictions: Map<String, Int>
    ) : FileDocumentProcessingResultDto(), DocumentProcessingResult {
        constructor(
            file: File,
            document: ExtractedDocument,
            predictions: Map<String, Int>
        ) : this(file.name, document.metadata, predictions)
    }

    data class Failure(override val filename: String, val errorMessage: String) : FileDocumentProcessingResultDto() {
        constructor(file: File, error: Throwable) : this(file.name, error.rootCause.message ?: "Unexpected error")
    }
}
