package pl.piasta.newswise.classification.processing

import edu.stanford.nlp.ling.CoreLabel
import edu.stanford.nlp.pipeline.StanfordCoreNLP
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import org.springframework.context.annotation.Lazy
import pl.piasta.newswise.common.isStopWord
import pl.piasta.newswise.common.normalize
import pl.piasta.newswise.common.removeAllWhitespaces
import pl.piasta.newswise.common.removeAlphanumerics
import pl.piasta.newswise.common.removeNonLetters
import pl.piasta.newswise.common.removeRepeatedCharacters
import pl.piasta.newswise.common.removeSingleCharacterWords
import pl.piasta.newswise.common.removeUrls
import pl.piasta.newswise.common.tokens

interface TextProcessor {
    suspend fun process(text: String): String
}

class CoreNLPTextProcessor(
    @Lazy private val coreNLP: StanfordCoreNLP,
    private val contractionsExpander: ContractionsExpander,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : TextProcessor {
    override suspend fun process(text: String) = coroutineScope {
        text.preprocessText()
            .pipe()
            .expandContractions()
            .pipe()
            .filterNot { it.isStopWord() }
            .map { it.postprocessToken() }
            .filterNot { it.isBlank() }
            .joinToString(" ")
    }

    private fun String.preprocessText() = removeUrls()
        .lowercase()
        .normalize()
        .removeAlphanumerics()
        .removeRepeatedCharacters()
        .removeSingleCharacterWords()
        .trim()

    private fun CoreLabel.postprocessToken() = lemma()
        .lowercase()
        .removeNonLetters()
        .removeSingleCharacterWords()
        .removeAllWhitespaces()

    private suspend fun List<CoreLabel>.expandContractions() = associate { it.word() to it.tag() }.let {
        contractionsExpander.expand(it)
    }

    private suspend fun String.pipe() = withContext(dispatcher) {
        coreNLP.process(this@pipe).tokens()
    }
}
