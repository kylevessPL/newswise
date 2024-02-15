#!/bin/sh

///bin/true <<EOC
/*
EOC
attempt=1
while true; do
    kotlin "$0" "$@"
    exit_code="$?"
    if [ "$exit_code" -eq 255 ] && [ "$attempt" -eq 1 ]; then
        attempt=$((attempt + 1))
    else
        exit "$exit_code"
    fi
done
*/

@file:Import("feature_generators.kt")
@file:DependsOn("org.apache.opennlp:opennlp-tools:2.3.0")
@file:CompilerOptions("-jvm-target", "19")

import java.io.File
import java.io.IOException
import java.lang.System.err
import java.lang.System.getenv
import java.lang.System.out
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.system.exitProcess
import kotlin.text.Charsets.UTF_8
import opennlp.tools.cmdline.doccat.DoccatFineGrainedReportListener
import opennlp.tools.doccat.BagOfWordsFeatureGenerator
import opennlp.tools.doccat.DoccatCrossValidator
import opennlp.tools.doccat.DoccatFactory
import opennlp.tools.doccat.DocumentCategorizerME
import opennlp.tools.doccat.DocumentSample
import opennlp.tools.doccat.DocumentSampleStream
import opennlp.tools.doccat.FeatureGenerator
import opennlp.tools.doccat.NGramFeatureGenerator
import opennlp.tools.ml.naivebayes.NaiveBayesTrainer.NAIVE_BAYES_VALUE
import opennlp.tools.tokenize.TokenizerME
import opennlp.tools.util.DownloadUtil
import opennlp.tools.util.FilterObjectStream
import opennlp.tools.util.MarkableFileInputStreamFactory
import opennlp.tools.util.PlainTextByLineStream
import opennlp.tools.util.TrainingParameters
import opennlp.tools.util.TrainingParameters.ALGORITHM_PARAM
import opennlp.tools.util.TrainingParameters.CUTOFF_PARAM
import opennlp.tools.util.ext.ExtensionLoader

val classLoaderExitCode = -1

val language = "en"
val nFolds = 5

val tokenizer = TokenizerME(language)

val params = TrainingParameters(
    mapOf(
        ALGORITHM_PARAM to NAIVE_BAYES_VALUE,
        CUTOFF_PARAM to 0
    )
)

val listener = DoccatFineGrainedReportListener(out)

@Suppress("UNRESOLVED_REFERENCE")
val customGenerators = arrayOf<FeatureGenerator>(FirstLastWordsFeatureGenerator()).onEach {
    runCatching {
        Class.forName(it.javaClass.name, false, ExtensionLoader::class.java.classLoader)
    }.getOrElse { exitProcess(classLoaderExitCode) }
}

val doccatFactory = DoccatFactory(
    arrayOf(
        BagOfWordsFeatureGenerator(),
        NGramFeatureGenerator(),
        *customGenerators
    )
)

val dataPath: Path = getenv("DATA_PATH")
    ?.let { Path(it) }
    ?: error("DATA_PATH environment variable is not set!")

val datasetFile: File = dataPath.resolve("data_opennlp.dat").toFile()
val modelFile: File = dataPath.resolve("model_opennlp.bin").toFile()

val validator = DoccatCrossValidator(language, params, doccatFactory, listener)
val dataIn = MarkableFileInputStreamFactory(datasetFile)

fun printErr(message: String) = err.println(message)

fun String.tokenize(): Array<String> = tokenizer.tokenize(this)

fun String.halve() = split(" ", limit = 2).takeIf { it.size == 2 }?.let { it[0] to it[1] }

fun train() = PlainTextByLineStream(dataIn, UTF_8).use {
    object : DocumentSampleStream(it) {
        override fun read() = samples.read()?.let { sample ->
            sample.trim().halve()?.let { (category, text) ->
                val tokens = text.tokenize()
                DocumentSample(category, tokens)
            } ?: throw IOException("Empty lines, or lines with only a category string are not allowed!")
        }
    }.use { stream ->
//        validator.evaluate(stream, nFolds)
//        listener.writeReport()
        DocumentCategorizerME.train(language, stream, params, doccatFactory).serialize(modelFile)
    }
}

runCatching {
    println("Training started (OpenNLP).")
    train()
}.onSuccess {
    println("Training finished with success.")
}.onFailure {
    printErr("Training finished with failure.")
}.getOrThrow()
