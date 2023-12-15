package pl.piasta.newswise.processing

import pl.piasta.newswise.classification.NewsArticleClassifier
import pl.piasta.newswise.classification.corenlp.CoreNLPNewsArticleClassifier
import pl.piasta.newswise.classification.opennlp.OpenNLPBERTNewsArticleClassifier
import pl.piasta.newswise.classification.opennlp.OpenNLPMENewsArticleClassifier
import pl.piasta.newswise.classification.weka.WekaNewsArticleClassifier
import pl.piasta.newswise.processing.CategorizerModel.CORENLP_ME
import pl.piasta.newswise.processing.CategorizerModel.OPENNLP_BERT
import pl.piasta.newswise.processing.CategorizerModel.OPENNLP_ME
import pl.piasta.newswise.processing.CategorizerModel.WEKA_ME

interface DocumentCategorizationManager {
    suspend fun categorize(document: String, model: CategorizerModel): Map<String, Int>
}

class NewsArticleCategorizationManager(
    private val openNLPBERTNewsArticleClassifier: OpenNLPBERTNewsArticleClassifier,
    private val openNLPMENewsArticleClassifier: OpenNLPMENewsArticleClassifier,
    private val coreNLPNewsArticleClassifier: CoreNLPNewsArticleClassifier,
    private val wekaNewsArticleClassifier: WekaNewsArticleClassifier
) : DocumentCategorizationManager {
    private val CategorizerModel.classifier: NewsArticleClassifier
        get() = when (this) {
            OPENNLP_BERT -> openNLPBERTNewsArticleClassifier
            OPENNLP_ME -> openNLPMENewsArticleClassifier
            CORENLP_ME -> coreNLPNewsArticleClassifier
            WEKA_ME -> wekaNewsArticleClassifier
        }

    override suspend fun categorize(document: String, model: CategorizerModel) = model.classifier
        .classify(document)
        .mapKeys { it.key.category }
}
