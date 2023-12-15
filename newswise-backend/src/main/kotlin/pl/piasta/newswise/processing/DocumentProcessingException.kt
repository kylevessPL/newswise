package pl.piasta.newswise.processing

import pl.piasta.newswise.processing.DocumentProcessingError.INVALID_DOCUMENT_URL
import pl.piasta.newswise.processing.DocumentProcessingError.INVALID_NEWS_ARTICLE_DOCUMENT
import pl.piasta.newswise.processing.DocumentProcessingError.UNSUPPORTED_DOCUMENT_LANGUAGE
import pl.piasta.newswise.processing.DocumentProcessingError.UNSUPPORTED_DOCUMENT_TYPE

private enum class DocumentProcessingError {
    UNSUPPORTED_DOCUMENT_TYPE,
    UNSUPPORTED_DOCUMENT_LANGUAGE,
    INVALID_DOCUMENT_URL,
    INVALID_NEWS_ARTICLE_DOCUMENT
}

sealed class DocumentProcessingException(code: DocumentProcessingError) : Exception(code.name) {
    class UnsupportedDocumentTypeException : DocumentProcessingException(UNSUPPORTED_DOCUMENT_TYPE)
    class UnsupportedDocumentLanguageException : DocumentProcessingException(UNSUPPORTED_DOCUMENT_LANGUAGE)
    class InvalidDocumentURLException : DocumentProcessingException(INVALID_DOCUMENT_URL)
    class InvalidNewsArticleDocumentException : DocumentProcessingException(INVALID_NEWS_ARTICLE_DOCUMENT)
}
