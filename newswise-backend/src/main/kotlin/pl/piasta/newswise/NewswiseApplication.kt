package pl.piasta.newswise

import kotlin.io.path.copyToRecursively
import org.bytedeco.javacpp.Loader
import org.bytedeco.leptonica.global.leptonica
import org.bytedeco.tesseract.program.tesseract
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.support.GenericApplicationContext
import pl.piasta.newswise.api.apiConfig
import pl.piasta.newswise.classification.classificationConfig
import pl.piasta.newswise.common.toPath
import pl.piasta.newswise.configuration.commonConfig
import pl.piasta.newswise.extraction.extractionConfig
import pl.piasta.newswise.processing.processingConfig

@SpringBootApplication
@ConfigurationPropertiesScan
class NewswiseApplication

@Suppress("unused")
class BeansInitializer : ApplicationContextInitializer<GenericApplicationContext> {
    override fun initialize(applicationContext: GenericApplicationContext) =
        arrayOf(commonConfig, apiConfig, processingConfig, extractionConfig, classificationConfig).forEach {
            it.initialize(applicationContext)
        }
}

private fun initializeTesseract() {
    val leptonicaPath = Loader.load(leptonica::class.java).toPath().parent
    val tesseractPath = Loader.load(tesseract::class.java).toPath().parent
    leptonicaPath.copyToRecursively(tesseractPath, followLinks = true, overwrite = true)
}

fun main(args: Array<String>) {
    runApplication<NewswiseApplication>(*args) {
        initializeTesseract()
    }
}
