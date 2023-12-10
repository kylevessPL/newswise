package pl.piasta.newswise.extraction

import com.github.pemistahl.lingua.api.IsoCode639_3
import com.github.pemistahl.lingua.api.IsoCode639_3.POL
import com.github.pemistahl.lingua.api.LanguageDetector
import com.github.pemistahl.lingua.api.LanguageDetectorBuilder
import org.apache.tika.config.TikaConfig
import org.apache.tika.parser.AutoDetectParser
import org.apache.tika.parser.ParseContext
import org.apache.tika.parser.Parser
import org.apache.tika.parser.ocr.TesseractOCRConfig
import org.apache.tika.parser.pdf.PDFParserConfig
import org.bytedeco.javacpp.Loader
import org.bytedeco.tesseract.program.tesseract
import org.languagetool.Language
import org.languagetool.language.AmericanEnglish
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.support.beans
import org.springframework.core.io.ResourceLoader
import pl.piasta.newswise.common.absolutePathString
import pl.piasta.newswise.common.fromString
import pl.piasta.newswise.common.reader
import pl.piasta.newswise.common.resolvePlaceholders
import pl.piasta.newswise.common.toPath
import pl.piasta.newswise.common.toStringProperties

@ConfigurationProperties("app.extraction")
data class ExtractionProperties(val languages: List<String>, val tika: TikaProperties)

data class TikaProperties(val configTemplatePath: String, val tesseract: TesseractProperties)

data class TesseractProperties(val dataPath: String)

val extractionConfig = beans {
    bean<TikaDocumentExtractor>()
    bean<DocumentMetadataExtractor>()
    bean<Language> { AmericanEnglish() }
    bean<LanguageDetector> {
        val supportedLanguages = ref<ExtractionProperties>().languages
            .map { fromString<IsoCode639_3>(it) }
            .toTypedArray()
        LanguageDetectorBuilder
            .fromIsoCodes639_3(POL, *supportedLanguages)
            .withPreloadedLanguageModels()
            .build()
    }
    bean<ParseContext> {
        val properties = ref<ExtractionProperties>().tika
        val tikaProperties = mapOf(
            "tesseract.library" to Loader.load(tesseract::class.java).toPath().parent,
            "tesseract.data" to ref<ResourceLoader>().absolutePathString(properties.tesseract.dataPath)!!
        ).toStringProperties()
        val tikaConfig = ref<ResourceLoader>()
            .reader(properties.configTemplatePath)
            .use { it!!.readText() }
            .resolvePlaceholders(tikaProperties)
            .byteInputStream()
            .use { TikaConfig(it) }
        val pdfConfig = PDFParserConfig().apply {
            isExtractInlineImages = true
            isExtractUniqueInlineImagesOnly = true
        }
        val tesseractConfig = TesseractOCRConfig().apply {
            language = ref<ExtractionProperties>().languages.joinToString("+")
        }
        ParseContext().apply {
            set(Parser::class.java, AutoDetectParser(tikaConfig))
            set(PDFParserConfig::class.java, pdfConfig)
            set(TesseractOCRConfig::class.java, tesseractConfig)
        }
    }
}
