package pl.piasta.newswise.classification.opennlp

import opennlp.tools.tokenize.TokenizerME
import opennlp.tools.tokenize.TokenizerModel

fun interface OpenNLPTokenizer {
    fun tokenize(text: String): Array<String>
}

class TokenizerMEWrapper(private val tokenizerModel: TokenizerModel) : OpenNLPTokenizer {
    private val tokenizer: TokenizerME
        get() = TokenizerME(tokenizerModel)

    override fun tokenize(text: String): Array<String> = tokenizer.tokenize(text)
}
