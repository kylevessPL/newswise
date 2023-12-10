package pl.piasta.newswise.processing

sealed class DocumentProcessingException(override val message: String) : Exception(message) {
    class UnsupportedDocumentTypeException : DocumentProcessingException("Unsupported document type")
    class UnsupportedDocumentLanguageException : DocumentProcessingException("Unsupported document language")
    class InvalidNewsArticleDocumentException :
        DocumentProcessingException("Document content is either empty or document is not a valid news article")
}
