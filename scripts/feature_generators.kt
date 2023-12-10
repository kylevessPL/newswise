import opennlp.tools.doccat.FeatureGenerator

class FirstLastWordsFeatureGenerator : FeatureGenerator {
    override fun extractFeatures(text: Array<String>, extraInformation: Map<String, Any>) =
        listOf("fw=${text.first()}", "lw=${text.last()}")
}
