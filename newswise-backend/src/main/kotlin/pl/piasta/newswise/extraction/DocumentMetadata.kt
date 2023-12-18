@file:Suppress("unused")

package pl.piasta.newswise.extraction

import java.time.Instant
import kotlin.reflect.KClass
import org.apache.tika.metadata.Office
import org.apache.tika.metadata.PDF
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

enum class ImageResolutionUnit { INCH, CM }

enum class DocumentMetadata(
    val type: KClass<*>,
    vararg val property: String,
    val customMapper: ((String) -> Any?)? = null
) {
    TITLE(String::class, TikaCoreProperties.TITLE.name, "Exif IFD0:Windows XP Title"),
    DESCRIPTION(String::class, TikaCoreProperties.DESCRIPTION.name, "Exif IFD0:Image Description"),
    COMMENT(String::class, "Exif IFD0:Windows XP Comment", TikaCoreProperties.COMMENTS.name),
    SUBJECT(String::class, TikaCoreProperties.SUBJECT.name, "Exif IFD0:Windows XP Subject"),
    KEYWORDS(String::class, Office.KEYWORDS.name, PDF.DOC_INFO_KEY_WORDS.name, "Exif IFD0:Windows XP Keywords"),
    CREATOR(String::class, TikaCoreProperties.CREATOR.name, "Exif IFD0:Artist", "Exif IFD0:Windows XP Author"),
    INITIAL_CREATOR(String::class, Office.INITIAL_AUTHOR.name),
    CREATION_DATE(
        Instant::class,
        TikaCoreProperties.CREATED.name,
        "Exif SubIFD:Date/Time Original",
        "Exif SubIFD:Date/Time Digitized"
    ),
    MODIFIER(String::class, TikaCoreProperties.MODIFIER.name),
    MODIFICATION_DATE(Instant::class, TikaCoreProperties.MODIFIED.name, "File Modified Date"),
    CONTRIBUTOR(String::class, TikaCoreProperties.CONTRIBUTOR.name),
    PUBLISHER(String::class, TikaCoreProperties.PUBLISHER.name),
    PAGE_COUNT(Int::class, Office.PAGE_COUNT.name),
    PARAGRAPH_COUNT(Int::class, Office.PARAGRAPH_COUNT.name),
    CHARACTER_COUNT(Int::class, Office.CHARACTER_COUNT.name),
    CHARACTER_COUNT_WITH_SPACES(Int::class, Office.CHARACTER_COUNT_WITH_SPACES.name),
    WORD_COUNT(Int::class, Office.WORD_COUNT.name),
    LINE_COUNT(Int::class, Office.LINE_COUNT.name),
    TABLE_COUNT(Int::class, Office.TABLE_COUNT.name),
    IMAGE_COUNT(Int::class, Office.IMAGE_COUNT.name, TikaCoreProperties.NUM_IMAGES.name),
    BITS_PER_SAMPLE(Int::class, TIFF.BITS_PER_SAMPLE.name),
    IMAGE_HEIGHT(Int::class, TIFF.IMAGE_LENGTH.name),
    IMAGE_WIDTH(Int::class, TIFF.IMAGE_WIDTH.name),
    SAMPLES_PER_PIXEL(Int::class, TIFF.SAMPLES_PER_PIXEL.name),
    FLASH_FIRED(Boolean::class, TIFF.FLASH_FIRED.name, "Exif SubIFD:Flash", customMapper = ::mapFlashFired),
    EXPOSURE_TIME(Double::class, TIFF.EXPOSURE_TIME.name),
    F_NUMBER(Double::class, TIFF.F_NUMBER.name),
    FOCAL_LENGTH(Double::class, TIFF.FOCAL_LENGTH.name),
    ISO_SPEED_RATINGS(Int::class, TIFF.ISO_SPEED_RATINGS.name),
    EQUIPMENT_MAKE(String::class, TIFF.EQUIPMENT_MAKE.name, "Exif IFD0:Make"),
    EQUIPMENT_MODEL(String::class, TIFF.EQUIPMENT_MODEL.name, "Exif IFD0:Model"),
    SOFTWARE(String::class, TIFF.SOFTWARE.name),
    ORIENTATION(ImageOrientation::class, TIFF.ORIENTATION.name, customMapper = ::mapOrientation),
    RESOLUTION_HORIZONTAL(Double::class, TIFF.RESOLUTION_HORIZONTAL.name),
    RESOLUTION_VERTICAL(Double::class, TIFF.RESOLUTION_VERTICAL.name),
    RESOLUTION_UNIT(ImageResolutionUnit::class, TIFF.RESOLUTION_UNIT.name, "Resolution Units")
}

fun mapFlashFired(value: String): Any? = value.toBooleanStrictOrNull()?.let {
    value.takeIf { it == "Flash fired" }
}

fun mapOrientation(value: String): Any? = value.toIntOrNull()?.minus(1)?.runCatching {
    ImageOrientation.entries[this]
}?.getOrNull()
