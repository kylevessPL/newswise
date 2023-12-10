package pl.piasta.newswise.classification.corenlp

import edu.stanford.nlp.classify.ColumnDataClassifier
import edu.stanford.nlp.ling.Datum
import edu.stanford.nlp.ling.RVFDatum
import edu.stanford.nlp.stats.Counter
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import org.springframework.context.annotation.Lazy
import pl.piasta.newswise.classification.DocumentCategory
import pl.piasta.newswise.classification.NewsArticleClassifier
import pl.piasta.newswise.classification.processing.TextProcessor

class CoreNLPNewsArticleClassifier(
    textProcessor: TextProcessor,
    @Lazy private val classifier: ColumnDataClassifier,
    @Lazy private val featureCounter: Counter<String>,
    private val properties: CoreNLPProperties,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default
) : NewsArticleClassifier(textProcessor) {
    override suspend fun predict(text: String): Map<DocumentCategory, Double> = coroutineScope {
        text.createDatum().score()
    }

    private suspend fun String.createDatum() = withContext(dispatcher) {
        classifier.makeDatumFromLine(this@createDatum)
            .applyTFIDFl1Normalization()
            .applyFeatureWeightThreshold()
    }

    private suspend fun RVFDatum<String, String>.score(): Map<DocumentCategory, Double> = withContext(dispatcher) {
        val scores = classifier.scoresOf(this@score)
            .entrySet()
            .associate { it.key.newsCategory to it.value }
        val probabilities = CoreNLPHelper.softmax(scores.values)
        scores.keys.zip(probabilities).toMap()
    }

    private suspend fun Datum<String, String>.applyTFIDFl1Normalization() =
        CoreNLPHelper.l1NormalizeTFIDF(this, featureCounter, dispatcher)

    private suspend fun RVFDatum<String, String>.applyFeatureWeightThreshold() =
        CoreNLPHelper.selectFeaturesByThreshold(this, properties.featureWeightThreshold, dispatcher)
}
