package pl.piasta.newswise.processing

import pl.piasta.newswise.common.rootCause

private interface DocumentProcessingResult {
    val metadata: Map<String, Any>
    val predictions: Map<String, Int>
}

data class RemoteDocumentProcessingResultDto(
    override val metadata: Map<String, Any>,
    override val predictions: Map<String, Int>
) : DocumentProcessingResult

sealed class FileDocumentProcessingResultDto {
    abstract val name: String

    data class Success(
        override val name: String,
        override val metadata: Map<String, Any>,
        override val predictions: Map<String, Int>
    ) : FileDocumentProcessingResultDto(), DocumentProcessingResult

    data class Failure(override val name: String, val errorMessage: String) : FileDocumentProcessingResultDto() {
        constructor(name: String, error: Throwable) : this(name, error.rootCause.message ?: "Unexpected error")
    }
}
