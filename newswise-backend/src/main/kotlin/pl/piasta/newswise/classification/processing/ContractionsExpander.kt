package pl.piasta.newswise.classification.processing

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import pl.piasta.newswise.common.equalsIgnoreCase

fun interface ContractionsExpander {
    suspend fun expand(posTaggedTokens: Map<String, String>): String
}

class EnglishContractionsExpander(private val dispatcher: CoroutineDispatcher = Dispatchers.Default) :
    ContractionsExpander {
    override suspend fun expand(posTaggedTokens: Map<String, String>) = withContext(dispatcher) {
        val newTokens = mutableListOf<String>()
        val tokens = posTaggedTokens.keys.toTypedArray()
        val tags = posTaggedTokens.values.toTypedArray()
        val len = posTaggedTokens.size
        var i = 0
        while (i < len) {
            val contraction = when {
                i == len - 1 -> contractionFor(tokens[len - 1])
                else -> contractionFor(tokens[i], tokens[i + 1], tags[i + 1])
            }
            newTokens += contraction.drop(1)
            i += contraction.first().toInt()
        }
        newTokens.joinToString(" ")
    }

    private fun contractionFor(word: String) = when {
        word.equalsIgnoreCase("'ll") -> arrayOf("1", "will")
        word.equalsIgnoreCase("'re") -> arrayOf("1", "are")
        word.equalsIgnoreCase("'ve") -> arrayOf("1", "have")
        word.equalsIgnoreCase("gonna") -> arrayOf("1", "going", "to")
        word.equalsIgnoreCase("gotta") -> arrayOf("1", "got", "to")
        word.equalsIgnoreCase("'m") -> arrayOf("1", "am")
        word.equalsIgnoreCase("o'clock") -> arrayOf("1", "of", "the", "clock")
        word.equalsIgnoreCase("ma'am") -> arrayOf("1", "madam")
        word.equalsIgnoreCase("'tis") -> arrayOf("1", "it", "is")
        word.equalsIgnoreCase("'twas") -> arrayOf("1", "it", "was")
        word.equalsIgnoreCase("'d") -> arrayOf("1", "would")
        word.equalsIgnoreCase("n't") -> arrayOf("1", "not")
        else -> arrayOf("1", word)
    }

    private fun contractionFor(word: String, nextWord: String, nextPos: String) = when {
        word.equalsIgnoreCase("'ll") -> arrayOf("1", "will")
        word.equalsIgnoreCase("'re") -> arrayOf("1", "are")
        word.equalsIgnoreCase("'ve") -> arrayOf("1", "have")
        word.equalsIgnoreCase("gonna") -> arrayOf("1", "going", "to")
        word.equalsIgnoreCase("gotta") -> arrayOf("1", "got", "to")
        word.equalsIgnoreCase("'m") -> arrayOf("1", "am")
        word.equalsIgnoreCase("o'clock") -> arrayOf("1", "of", "the", "clock")
        word.equalsIgnoreCase("ma'am") -> arrayOf("1", "madam")
        word.equalsIgnoreCase("'tis") -> arrayOf("1", "it", "is")
        word.equalsIgnoreCase("'twas") -> arrayOf("1", "it", "was")
        word.equalsIgnoreCase("'s") and nextPos.equalsIgnoreCase("VBG") -> arrayOf("1", "is")
        word.equalsIgnoreCase("'s") and nextPos.equalsIgnoreCase("DT") -> arrayOf("1", "has")
        word.equalsIgnoreCase("'s") and nextPos.equalsIgnoreCase("JJ") -> arrayOf("1", "is")
        word.equalsIgnoreCase("'s") and nextPos.startsWith("JJ") -> arrayOf("1", "is")
        word.equalsIgnoreCase("'s") and nextPos.startsWith("RB") -> arrayOf("1", "is")
        word.equalsIgnoreCase("'s") and nextPos.startsWith("VB") and nextWord.endsWith("ing", true) ->
            arrayOf("1", "is")

        word.equalsIgnoreCase("'d") and (nextPos == "VBN") -> arrayOf("1", "had")
        word.equalsIgnoreCase("'d") -> arrayOf("1", "would")
        word.equalsIgnoreCase("ne") and nextWord.equalsIgnoreCase("'er") -> arrayOf("2", "never")
        word.equalsIgnoreCase("o") and nextWord.equalsIgnoreCase("'er") -> arrayOf("2", "over")
        word.equalsIgnoreCase("y") and nextWord.equalsIgnoreCase("'all") -> arrayOf("2", "you", "all")
        word.equalsIgnoreCase("let") and nextWord.equalsIgnoreCase("'s") -> arrayOf("2", "let", "us")
        word.equalsIgnoreCase("ai") and nextWord.equalsIgnoreCase("n't") -> arrayOf("2", "is", "not")
        word.equalsIgnoreCase("ca") and nextWord.equalsIgnoreCase("n't") -> arrayOf("2", "can", "not")
        word.equalsIgnoreCase("wo") and nextWord.equalsIgnoreCase("n't") -> arrayOf("2", "will", "not")
        word.equalsIgnoreCase("n't") -> arrayOf("1", "not")
        else -> arrayOf("1", word)
    }
}
