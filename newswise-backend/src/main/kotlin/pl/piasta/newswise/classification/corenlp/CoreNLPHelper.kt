package pl.piasta.newswise.classification.corenlp

import edu.stanford.nlp.classify.ColumnDataClassifier
import edu.stanford.nlp.classify.Dataset
import edu.stanford.nlp.io.IOUtils
import edu.stanford.nlp.ling.Datum
import edu.stanford.nlp.ling.RVFDatum
import edu.stanford.nlp.stats.Counter
import edu.stanford.nlp.util.ErasureUtils
import kotlin.math.exp
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object CoreNLPHelper {
    fun readClassifier(path: String): ColumnDataClassifier = ColumnDataClassifier.getClassifier(path)

    fun readFeatureCounter(path: String): Counter<String> = IOUtils.readStreamFromString(path).use {
        ErasureUtils.uncheckedCast(it.readObject())
    }

    fun softmax(logits: Collection<Double>): Collection<Double> {
        val expLogits = logits.map { exp(it) }
        val sumExp = expLogits.sum()
        return expLogits.map { it / sumExp }
    }

    suspend fun l1NormalizeTFIDF(
        datum: Datum<String, String>,
        featureDocCounts: Counter<String>,
        dispatcher: CoroutineDispatcher = Dispatchers.Default
    ): RVFDatum<String, String> = withContext(dispatcher) {
        Dataset<String, String>().apply { add(datum) }.getL1NormalizedTFIDFDatum(datum, featureDocCounts)
    }

    suspend fun selectFeaturesByThreshold(
        datum: RVFDatum<String, String>,
        featureWeightThreshold: Double,
        dispatcher: CoroutineDispatcher = Dispatchers.Default
    ) = withContext(dispatcher) {
        datum.apply {
            val maxWeight = asFeaturesCounter().values().max()
            val threshold = calculateThreshold(featureWeightThreshold, maxWeight)
            with(asFeaturesCounter()) {
                entrySet().filter { it.value < threshold }.forEach { remove(it.key) }
            }
        }
    }

    private fun calculateThreshold(featureWeightThreshold: Double, maxWeight: Double) =
        featureWeightThreshold.times(maxWeight).div(100)
}
