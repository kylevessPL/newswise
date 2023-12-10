@file:Suppress("unused")

package pl.piasta.newswise.extraction

import java.time.Instant
import kotlin.reflect.KClass
import org.apache.tika.metadata.Office
import org.apache.tika.metadata.PDF
import org.apache.tika.metadata.Property
import org.apache.tika.metadata.TIFF
import org.apache.tika.metadata.TikaCoreProperties

enum class ImageOrientation {
    TOP_LEFT,
    TOP_RIGHT,
    BOTTOM_RIGHT,
    BOTTOM_LEFT,
    LEFT_TOP,
    RIGHT_TOP,
    RIGHT_BOTTOM,
    LEFT_BOTTOM
}

enum class ImageResolutionUnit { INCHES, CENTIMETRES }

enum class DocumentMetadata(val type: KClass<*>, vararg val property: Property) {
    TITLE(String::class, TikaCoreProperties.TITLE),
    DESCRIPTION(String::class, TikaCoreProperties.DESCRIPTION),
    SUBJECT(String::class, PDF.DOC_INFO_SUBJECT),
    KEYWORDS(String::class, Office.KEYWORDS, PDF.DOC_INFO_KEY_WORDS),
    CREATOR(String::class, TikaCoreProperties.CREATOR),
    INITIAL_CREATOR(String::class, Office.INITIAL_AUTHOR),
    CREATION_DATE(Instant::class, TikaCoreProperties.CREATED),
    MODIFIER(String::class, TikaCoreProperties.MODIFIER),
    MODIFICATION_DATE(Instant::class, TikaCoreProperties.MODIFIED),
    CONTRIBUTOR(String::class, TikaCoreProperties.CONTRIBUTOR),
    PUBLISHER(String::class, TikaCoreProperties.PUBLISHER),
    PAGE_COUNT(Int::class, Office.PAGE_COUNT),
    PARAGRAPH_COUNT(Int::class, Office.PARAGRAPH_COUNT),
    CHARACTER_COUNT(Int::class, Office.CHARACTER_COUNT),
    CHARACTER_COUNT_WITH_SPACES(Int::class, Office.CHARACTER_COUNT_WITH_SPACES),
    WORD_COUNT(Int::class, Office.WORD_COUNT),
    LINE_COUNT(Int::class, Office.LINE_COUNT),
    TABLE_COUNT(Int::class, Office.TABLE_COUNT),
    IMAGE_COUNT(Int::class, Office.IMAGE_COUNT, TikaCoreProperties.NUM_IMAGES),
    BITS_PER_SAMPLE(Int::class, TIFF.BITS_PER_SAMPLE),
    IMAGE_HEIGHT(Int::class, TIFF.IMAGE_LENGTH),
    IMAGE_WIDTH(Int::class, TIFF.IMAGE_WIDTH),
    SAMPLES_PER_PIXEL(Int::class, TIFF.SAMPLES_PER_PIXEL),
    FLASH_FIRED(Boolean::class, TIFF.FLASH_FIRED),
    EXPOSURE_TIME(Double::class, TIFF.EXPOSURE_TIME),
    F_NUMBER(Double::class, TIFF.F_NUMBER),
    FOCAL_LENGTH(Double::class, TIFF.FOCAL_LENGTH),
    ISO_SPEED_RATINGS(Int::class, TIFF.ISO_SPEED_RATINGS),
    EQUIPMENT_MAKE(String::class, TIFF.EQUIPMENT_MAKE),
    EQUIPMENT_MODEL(String::class, TIFF.EQUIPMENT_MODEL),
    SOFTWARE(String::class, TIFF.SOFTWARE),
    ORIENTATION(ImageOrientation::class, TIFF.ORIENTATION),
    RESOLUTION_HORIZONTAL(Double::class, TIFF.RESOLUTION_HORIZONTAL),
    RESOLUTION_VERTICAL(Double::class, TIFF.RESOLUTION_VERTICAL),
    RESOLUTION_UNIT(ImageResolutionUnit::class, TIFF.RESOLUTION_UNIT)
}
