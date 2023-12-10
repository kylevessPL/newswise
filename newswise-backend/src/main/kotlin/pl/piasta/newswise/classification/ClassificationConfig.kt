package pl.piasta.newswise.classification

import edu.stanford.nlp.classify.ColumnDataClassifier
import edu.stanford.nlp.pipeline.StanfordCoreNLP
import edu.stanford.nlp.stats.Counter
import io.github.pepperkit.corenlp.stopwords.StopWordsAnnotator
import opennlp.dl.InferenceOptions
import opennlp.dl.doccat.DocumentCategorizerDL
import opennlp.dl.doccat.scoring.AverageClassificationScoringStrategy
import opennlp.tools.doccat.BagOfWordsFeatureGenerator
import opennlp.tools.doccat.DoccatModel
import opennlp.tools.doccat.DocumentCategorizerME
import opennlp.tools.doccat.FeatureGenerator
import opennlp.tools.doccat.NGramFeatureGenerator
import opennlp.tools.tokenize.TokenizerME
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.support.beans
import org.springframework.core.io.ResourceLoader
import pl.piasta.newswise.classification.corenlp.CoreNLPHelper
import pl.piasta.newswise.classification.corenlp.CoreNLPNewsArticleClassifier
import pl.piasta.newswise.classification.corenlp.CoreNLPProperties
import pl.piasta.newswise.classification.opennlp.OpenNLPBERTNewsArticleClassifier
import pl.piasta.newswise.classification.opennlp.OpenNLPMENewsArticleClassifier
import pl.piasta.newswise.classification.opennlp.OpenNLPProperties
import pl.piasta.newswise.classification.processing.CoreNLPTextProcessor
import pl.piasta.newswise.classification.processing.EnglishContractionsExpander
import pl.piasta.newswise.classification.processing.ProcessingProperties
import pl.piasta.newswise.classification.weka.WekaHelper
import pl.piasta.newswise.classification.weka.WekaNewsArticleClassifier
import pl.piasta.newswise.classification.weka.WekaProperties
import pl.piasta.newswise.common.absolutePathString
import pl.piasta.newswise.common.file
import pl.piasta.newswise.common.joinToString
import pl.piasta.newswise.common.toStringProperties
import pl.piasta.newswise.common.url
import weka.classifiers.meta.FilteredClassifier

@ConfigurationProperties("app.classification")
private data class ClassificationProperties(
    val processing: ProcessingProperties,
    val openNLP: OpenNLPProperties,
    val coreNLP: CoreNLPProperties,
    val weka: WekaProperties
)

val classificationConfig = beans {
    bean<WekaNewsArticleClassifier>()
    bean<OpenNLPBERTNewsArticleClassifier>()
    bean<OpenNLPMENewsArticleClassifier>()
    bean<CoreNLPNewsArticleClassifier>()
    bean<CoreNLPTextProcessor>()
    bean<EnglishContractionsExpander>()
    bean<CoreNLPProperties> { ref<ClassificationProperties>().coreNLP }
    bean<StanfordCoreNLP> {
        val properties = ref<ClassificationProperties>().processing
        val stopwords = ref<ResourceLoader>().absolutePathString(properties.stopwords)!!
        val annotators = listOf("tokenize", "ssplit", "pos", "lemma", "stopwords")
        val tokenizerOptions = mapOf(
            "quotes" to "ascii",
            "ellipses" to "unicode",
            "dashes" to "unicode",
            "untokenizable" to "noneDelete",
            "splitHyphenated" to false,
            "splitForwardSlash" to false,
            "normalizeCurrency" to true,
            "strictTreebank3" to true
        )
        val nlpProperties = mapOf(
            "annotators" to annotators.joinToString(),
            "customAnnotatorClass.stopwords" to StopWordsAnnotator::class.qualifiedName!!,
            "tokenize.options" to tokenizerOptions.joinToString(),
            "ssplit.isOneSentence" to true,
            "stopwords.customListFilePath" to stopwords
        )
        StanfordCoreNLP(nlpProperties.toStringProperties())
    }
    bean<TokenizerME>(isLazyInit = true) { TokenizerME("en") }
    bean<DocumentCategorizerDL>(isLazyInit = true) {
        val properties = ref<ClassificationProperties>().openNLP.dl
        val modelFile = ref<ResourceLoader>().file(properties.model)!!
        val vocabFile = ref<ResourceLoader>().file(properties.vocab)!!
        val configFile = ref<ResourceLoader>().file(properties.config)!!
        val scoringStrategy = AverageClassificationScoringStrategy()
        val inferenceOptions = InferenceOptions().apply { isIncludeTokenTypeIds = false }
        DocumentCategorizerDL(modelFile, vocabFile, configFile, scoringStrategy, inferenceOptions)
    }
    bean<DocumentCategorizerME>(isLazyInit = true) {
        val properties = ref<ClassificationProperties>().openNLP.me
        val modelUrl = ref<ResourceLoader>().url(properties.model)!!
        val bagOfWordsGenerator = BagOfWordsFeatureGenerator()
        val biGramGenerator = NGramFeatureGenerator()
        val firstLastWordsGenerator = FeatureGenerator { text, _ -> listOf("fw=${text.first()}", "lw=${text.last()}") }
        val model = DoccatModel(modelUrl).apply {
            factory.featureGenerators = arrayOf(bagOfWordsGenerator, biGramGenerator, firstLastWordsGenerator)
        }
        DocumentCategorizerME(model)
    }
    bean<ColumnDataClassifier>(isLazyInit = true) {
        val properties = ref<ClassificationProperties>().coreNLP
        val modelAbsolutePath = ref<ResourceLoader>().absolutePathString(properties.model)!!
        CoreNLPHelper.readClassifier(modelAbsolutePath)
    }
    bean<Counter<String>>(isLazyInit = true) {
        val properties = ref<ClassificationProperties>().coreNLP
        val modelAbsolutePath = ref<ResourceLoader>().absolutePathString(properties.counter)!!
        CoreNLPHelper.readFeatureCounter(modelAbsolutePath)
    }
    bean<FilteredClassifier>(isLazyInit = true) {
        val properties = ref<ClassificationProperties>().weka
        val modelAbsolutePath = ref<ResourceLoader>().file(properties.model)!!
        WekaHelper.readClassifier(modelAbsolutePath)
    }
}
