package pl.piasta.newswise.configuration

import org.springframework.context.support.beans
import org.springframework.core.io.DefaultResourceLoader
import org.springframework.core.io.ResourceLoader
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.reactive.CorsWebFilter
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource

val commonConfig = beans {
    bean<ResourceLoader> { DefaultResourceLoader() }
    bean<CorsWebFilter> {
        val properties = ref<ConfigProperties>().cors
        val corsConfig = CorsConfiguration().apply {
            maxAge = properties.maxAge
            allowedOrigins = properties.allowedOrigins
            allowedMethods = properties.allowedMethods
            allowedHeaders = properties.allowedHeaders
            exposedHeaders = properties.exposedHeaders
        }
        val source = UrlBasedCorsConfigurationSource().apply {
            registerCorsConfiguration("/**", corsConfig)
        }
        CorsWebFilter(source)
    }
}
