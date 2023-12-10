package pl.piasta.newswise.processing

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.support.beans

@ConfigurationProperties("app.processing")
data class ProcessingProperties(val contentTypes: List<String>)

val processingConfig = beans {
    bean<DocumentProcessingService>()
    bean<NewsArticleCategorizationManager>()
}
