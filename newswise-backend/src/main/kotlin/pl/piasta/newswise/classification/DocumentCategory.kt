package pl.piasta.newswise.classification

interface DocumentCategory {
    val category: String
}

enum class NewsArticleCategory(override val category: String) : DocumentCategory {
    BUSINESS_MONEY_LABEL("Business&Money"),
    CRIME_LEGAL_LABEL("Crime&Legal"),
    ENTERTAINMENT_ARTS_LABEL("Entertainment&Arts"),
    LIFESTYLE_LABEL("Lifestyle"),
    SCI_TECH_EDUCATION_LABEL("SciTech&Education"),
    SOCIETY_RELIGION_LABEL("Society&Religion"),
    SPORTS_HEALTH_LABEL("Sports&Health"),
    TRAVEL_FOOD_LABEL("Travel&Food"),
    WORLD_POLITICS_LABEL("World&Politics");

    companion object {
        val categories = entries.map { it.category }

        fun fromCategory(category: String) = entries.find { it.category == category }
    }
}
