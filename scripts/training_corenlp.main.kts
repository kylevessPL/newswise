#!/bin/sh

///bin/true <<EOC
/*
EOC
kotlin "$0" "$@"
exit $?
*/

@file:DependsOn("edu.stanford.nlp:stanford-corenlp:4.5.5")
@file:CompilerOptions("-jvm-target", "19")

import Training_corenlp_main.DataInfo
import edu.stanford.nlp.classify.ColumnDataClassifier
import edu.stanford.nlp.classify.Dataset
import edu.stanford.nlp.classify.GeneralDataset
import edu.stanford.nlp.classify.RVFDataset
import edu.stanford.nlp.io.IOUtils
import edu.stanford.nlp.ling.Datum
import edu.stanford.nlp.ling.RVFDatum
import edu.stanford.nlp.stats.Counter
import edu.stanford.nlp.util.Pair
import java.lang.System.err
import java.lang.System.getenv
import java.lang.System.setProperty
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString

typealias DataInfo = Pair<GeneralDataset<String, String>, List<Array<String>>>

mapOf(
    "org.slf4j.simpleLogger.logFile" to "System.out",
    "org.slf4j.simpleLogger.showLogName" to false,
    "org.slf4j.simpleLogger.showThreadName" to false
).forEach { (key, value) -> setProperty(key, value.toString()) }

fun printErr(message: String) = err.println(message)

fun Map<String, Any>.toStringProperties() = mapValues { (_, value) -> value.toString() }.toProperties()

val dataPath: Path = getenv("DATA_PATH")
    ?.let { Path(it) }
    ?: error("DATA_PATH environment variable is not set!")

val datasetPath: Path = dataPath.resolve("data_corenlp.dat")
val modelPath: Path = dataPath.resolve("model_corenlp.ser.gz")
val counterPath: Path = dataPath.resolve("counter_corenlp.ser.gz")

val properties = mapOf(
    "splitWordsWithPTBTokenizer" to true,
    "useSplitWords" to true,
    "useSplitWordPairs" to true,
    "useSplitFirstLastWords" to true,
    "useSplitPrefixSuffixNGrams" to true,
    "useNB" to true,
    "sigma" to 1.0E-30,
    "featureWeightThreshold" to 0.25,
    "featureMinimumSupport" to 2,
    "goldAnswerColumn" to 1,
    "serializeTo" to modelPath.absolutePathString()
//    "exitAfterTrainingFeaturization" to true,
//    "crossValidationFolds" to 5
).toStringProperties()

val classifier = object : ColumnDataClassifier(properties) {
    private lateinit var counter: Counter<String>

    override fun serializeClassifier(serializeTo: String?) {
        super.serializeClassifier(serializeTo)
        serializeCounter()
    }

    override fun readAndReturnTrainingExamples(filename: String): DataInfo {
        val dataset = super.readAndReturnTrainingExamples(filename)
        counter = (dataset.first as Dataset).featureCounter
        return dataset.normalizeTFIDF()
    }

    override fun readTestExamples(filename: String) = super.readTestExamples(filename).normalizeTFIDF(true)

    private fun serializeCounter() {
        IOUtils.writeStreamFromString(counterPath.absolutePathString()).use {
            it.writeObject(counter)
        }
    }

    private fun DataInfo.normalizeTFIDF(
        inTestPhase: Boolean = false,
        withFeatureWeightThreshold: Boolean = false
    ): DataInfo {
        val dataset = first.asTFIDFDataset(inTestPhase).also {
            if (withFeatureWeightThreshold) it.applyFeatureWeightThreshold()
        }
        return Pair(dataset, second)
    }

    private fun GeneralDataset<String, String>.asTFIDFDataset(inTestPhase: Boolean) = (this as Dataset).run {
        val featureDocCounts = if (inTestPhase) counter else featureCounter
        (0 until size()).map { i ->
            getL1NormalizedTFIDFDatum(getDatum(i), featureDocCounts)
        }.fold(RVFDataset(size(), featureIndex, labelIndex)) { acc, rvfDatum ->
            acc.add(rvfDatum)
            acc
        }
    }

    private fun GeneralDataset<String, String>.applyFeatureWeightThreshold() {
        val maxWeight = flatMap { it.asFeaturesCounter().values() }.max()
        featureWeightThreshold(maxWeight)?.let { featureWeightThreshold ->
            val datums = map { it.removeFeaturesBelowThreshold(featureWeightThreshold) }.toList()
            setAll(datums)
        }
    }

    private fun featureWeightThreshold(maxWeight: Double) = properties["featureWeightThreshold"]?.toString()
        ?.toDouble()
        ?.times(maxWeight)
        ?.div(100)

    private fun RVFDatum<String, String>.removeFeaturesBelowThreshold(featureWeightThreshold: Double) = also {
        with(asFeaturesCounter()) {
            entrySet().filter { it.value < featureWeightThreshold }.forEach { remove(it.key) }
        }
    }

    private fun GeneralDataset<String, String>.setAll(data: List<Datum<String, String>>) {
        clear()
        addAll(data)
    }
}

fun train() {
    classifier.trainClassifier(datasetPath.absolutePathString())
}

runCatching {
    println("Training started (CoreNLP).")
    train()
}.onSuccess {
    println("Training finished with success.")
}.onFailure {
    printErr("Training finished with failure.")
}.getOrThrow()
