package pl.piasta.newswise.classification.corenlp

data class CoreNLPProperties(
    val model: String,
    val counter: String,
    val featureWeightThreshold: Double
)
