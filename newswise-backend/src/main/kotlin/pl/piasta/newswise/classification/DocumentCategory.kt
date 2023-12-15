package pl.piasta.newswise.classification

interface DocumentCategory {
    val category: String
}

enum class NewsArticleCategory(val label: String) : DocumentCategory {
    BUSINESS_MONEY("Business&Money"),
    CRIME_LEGAL("Crime&Legal"),
    ENTERTAINMENT_ARTS("Entertainment&Arts"),
    LIFESTYLE("Lifestyle"),
    SCI_TECH_EDUCATION("SciTech&Education"),
    SOCIETY_RELIGION("Society&Religion"),
    SPORTS_HEALTH("Sports&Health"),
    TRAVEL_FOOD("Travel&Food"),
    WORLD_POLITICS("World&Politics");

    override val category = name

    companion object {
        val categories = entries.map { it.label }

        fun fromCategory(category: String) = entries.find { it.label == category }
    }
}
