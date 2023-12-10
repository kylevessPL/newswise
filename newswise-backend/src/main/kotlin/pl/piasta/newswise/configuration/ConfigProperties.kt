package pl.piasta.newswise.configuration

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("app.config")
data class ConfigProperties(val cors: CorsProperties)

data class CorsProperties(
    val maxAge: Long,
    val allowedOrigins: List<String>,
    val allowedMethods: List<String>,
    val allowedHeaders: List<String>,
    val exposedHeaders: List<String>
)
