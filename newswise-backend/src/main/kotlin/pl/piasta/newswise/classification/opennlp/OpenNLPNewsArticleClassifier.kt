package pl.piasta.newswise.classification.opennlp

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import opennlp.dl.doccat.DocumentCategorizerDL
import opennlp.tools.doccat.DocumentCategorizer
import opennlp.tools.doccat.DocumentCategorizerME
import opennlp.tools.tokenize.TokenizerME
import opennlp.tools.tokenize.TokenizerModel
import org.springframework.context.annotation.Lazy
import pl.piasta.newswise.classification.DocumentCategory
import pl.piasta.newswise.classification.NewsArticleClassifier
import pl.piasta.newswise.classification.processing.TextProcessor

abstract class OpenNLPNewsArticleClassifier(
    textProcessor: TextProcessor,
    protected val dispatcher: CoroutineDispatcher,
    private val categorizer: DocumentCategorizer,
) : NewsArticleClassifier(textProcessor) {
    protected abstract suspend fun tokenize(text: String): Array<String>

    override suspend fun predict(text: String): Map<DocumentCategory, Double> = text
        .calculateProbabilities()
        .mapKeys { it.key.newsCategory }

    private suspend fun String.calculateProbabilities() = coroutineScope {
        val text = tokenize(this@calculateProbabilities)
        withContext(dispatcher) {
            categorizer.scoreMap(text)
        }
    }
}

class OpenNLPBERTNewsArticleClassifier(
    @Lazy categorizer: DocumentCategorizerDL,
    textProcessor: TextProcessor,
    dispatcher: CoroutineDispatcher = Dispatchers.Unconfined
) : OpenNLPNewsArticleClassifier(textProcessor, dispatcher, categorizer) {
    override suspend fun tokenize(text: String) = arrayOf(text)
}

class OpenNLPMENewsArticleClassifier(
    @Lazy private val tokenizerModel: TokenizerModel,
    @Lazy categorizer: DocumentCategorizerME,
    textProcessor: TextProcessor,
    dispatcher: CoroutineDispatcher = Dispatchers.Default
) : OpenNLPNewsArticleClassifier(textProcessor, dispatcher, categorizer) {
    private val tokenizer: TokenizerME
        get() = TokenizerME(tokenizerModel)

    override suspend fun tokenize(text: String): Array<String> = withContext(dispatcher) {
        tokenizer.tokenize(text)
    }
}
