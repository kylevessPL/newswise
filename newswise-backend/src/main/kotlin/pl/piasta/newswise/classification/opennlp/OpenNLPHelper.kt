package pl.piasta.newswise.classification.opennlp

import java.io.File
import opennlp.dl.InferenceOptions
import opennlp.dl.doccat.DocumentCategorizerDL
import opennlp.dl.doccat.scoring.AverageClassificationScoringStrategy
import opennlp.tools.doccat.BagOfWordsFeatureGenerator
import opennlp.tools.doccat.DoccatModel
import opennlp.tools.doccat.DocumentCategorizerME
import opennlp.tools.doccat.FeatureGenerator
import opennlp.tools.doccat.NGramFeatureGenerator
import pl.piasta.newswise.common.sevenZipSingleStream

object OpenNLPHelper {
    fun readDLClassifier(model: File, vocab: File, config: File): DocumentCategorizerDL {
        val scoringStrategy = AverageClassificationScoringStrategy()
        val inferenceOptions = InferenceOptions().apply { isIncludeTokenTypeIds = false }
        return DocumentCategorizerDL(model, vocab, config, scoringStrategy, inferenceOptions)
    }

    fun readMEClassifier(model: File) = model.sevenZipSingleStream().use {
        val bagOfWordsGenerator = BagOfWordsFeatureGenerator()
        val biGramGenerator = NGramFeatureGenerator()
        val firstLastWordsGenerator = FeatureGenerator { txt, _ -> listOf("fw=${txt.first()}", "lw=${txt.last()}") }
        val doccat = DoccatModel(it).apply {
            factory.featureGenerators = arrayOf(bagOfWordsGenerator, biGramGenerator, firstLastWordsGenerator)
        }
        DocumentCategorizerME(doccat)
    }
}
