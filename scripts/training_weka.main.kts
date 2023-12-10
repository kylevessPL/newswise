#!/bin/sh

///bin/true <<EOC
/*
EOC
kotlin "$0" "$@"
exit $?
*/

@file:DependsOn("nz.ac.waikato.cms.weka:weka-stable:3.8.6")
@file:CompilerOptions("-jvm-target", "19")

import java.nio.file.Path
import kotlin.Int.Companion.MAX_VALUE
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString
import kotlin.random.Random
import kotlin.random.asJavaRandom
import weka.classifiers.Evaluation
import weka.classifiers.bayes.NaiveBayesMultinomial
import weka.classifiers.meta.FilteredClassifier
import weka.core.Instances
import weka.core.SelectedTag
import weka.core.SerializationHelper
import weka.core.converters.ConverterUtils.DataSource
import weka.core.tokenizers.NGramTokenizer
import weka.filters.unsupervised.attribute.StringToWordVector
import weka.filters.unsupervised.attribute.StringToWordVector.FILTER_NORMALIZE_ALL
import weka.filters.unsupervised.attribute.StringToWordVector.TAGS_FILTER

fun printErr(message: String) = System.err.println(message)

val dataPath: Path = System.getenv("DATA_PATH")
    ?.let { Path(it) }
    ?: error("DATA_PATH environment variable is not set!")

val datasetPath: Path = dataPath.resolve("data_weka.arff")
val modelPath: Path = dataPath.resolve("model_weka.bin")

val data: Instances = DataSource.read(datasetPath.absolutePathString()).apply { setClassIndex(0) }

val nFolds = 5
val nGramMin = 1
val nGramMax = 2

val nGramTokenizer = NGramTokenizer().apply {
    nGramMinSize = nGramMin
    nGramMaxSize = nGramMax
}

val wordVectorFilter = StringToWordVector().apply {
    tokenizer = nGramTokenizer
    normalizeDocLength = SelectedTag(FILTER_NORMALIZE_ALL, TAGS_FILTER)
    wordsToKeep = MAX_VALUE
    tfTransform = true
    idfTransform = true
    outputWordCounts = true
    doNotOperateOnPerClassBasis = true
    attributeIndices = "last"
}

val classifier = FilteredClassifier().apply {
    filter = wordVectorFilter
    classifier = NaiveBayesMultinomial()
}

fun evaluate() = Evaluation(data).run {
    crossValidateModel(classifier, data, nFolds, Random(1).asJavaRandom())
    println(toSummaryString())
    println(toClassDetailsString())
    println(toMatrixString())
}

fun train() {
//    evaluate()
    classifier.run {
        buildClassifier(data)
        SerializationHelper.write(modelPath.absolutePathString(), this)
    }
}

runCatching {
    println("Training started (Weka).")
    train()
}.onSuccess {
    println("Training finished with success.")
}.onFailure {
    printErr("Training finished with failure.")
}.getOrThrow()
