package pl.piasta.newswise.classification.weka

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.context.annotation.Lazy
import pl.piasta.newswise.classification.DocumentCategory
import pl.piasta.newswise.classification.NewsArticleCategory
import pl.piasta.newswise.classification.NewsArticleClassifier
import pl.piasta.newswise.classification.processing.TextProcessor
import weka.classifiers.meta.FilteredClassifier

class WekaNewsArticleClassifier(
    textProcessor: TextProcessor,
    @Lazy private val classifier: FilteredClassifier,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default
) : NewsArticleClassifier(textProcessor) {
    private val attributes = WekaHelper.createAttributes(NewsArticleCategory.categories)

    override suspend fun predict(text: String): Map<DocumentCategory, Double> {
        val probabilities = text.calculateProbabilities()
        return NewsArticleCategory.entries
            .zip(probabilities.asIterable())
            .toMap()
    }

    private suspend fun String.calculateProbabilities() = withContext(dispatcher) {
        val instance = WekaHelper.createPreditionInstance(this@calculateProbabilities, attributes)
        classifier.distributionForInstance(instance)
    }
}
