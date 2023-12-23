package pl.piasta.newswise.classification

import kotlinx.coroutines.coroutineScope
import pl.piasta.newswise.classification.processing.TextProcessor
import pl.piasta.newswise.common.percentageDistribution

fun interface DocumentClassifier {
    suspend fun classify(text: String): Map<DocumentCategory, Int>
}

abstract class NewsArticleClassifier(private val textProcessor: TextProcessor) : DocumentClassifier {
    protected val String.newsCategory
        get() = NewsArticleCategory.fromCategory(this)!!

    override suspend fun classify(text: String): Map<DocumentCategory, Int> = coroutineScope {
        val processed = textProcessor.process(text)
        val predicitons = predict(processed)
        val percentages = percentageDistribution(predicitons.values.toDoubleArray())
        predicitons.keys.zip(percentages).toMap()
    }

    protected abstract suspend fun predict(text: String): Map<DocumentCategory, Double>
}
