package pl.piasta.newswise.classification.opennlp

data class OpenNLPProperties(val dl: OpeNLPDLProperties, val me: OpeNLPMEProperties)

data class OpeNLPDLProperties(
    val model: String,
    val vocab: String,
    val config: String
)

data class OpeNLPMEProperties(val model: String)
