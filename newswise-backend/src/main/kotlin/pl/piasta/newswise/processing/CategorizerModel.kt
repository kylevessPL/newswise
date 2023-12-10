package pl.piasta.newswise.processing

enum class CategorizerModel(val model: String) {
    OPENNLP_BERT("bert"),
    OPENNLP_ME("opennlp"),
    CORENLP_ME("corenlp"),
    WEKA_ME("weka");

    companion object {
        fun fromModel(name: String) = CategorizerModel.entries.find { it.model == name }
    }
}
