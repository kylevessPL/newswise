package pl.piasta.newswise.common

import java.io.File
import kotlin.io.path.createTempFile
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val TMPDIR_PROPERTY = "java.io.tmpdir"

/**
 * Creates a temporary file in the system's temporary directory or a subdirectory.
 *
 * @param filename The name of the temporary file. If null, a unique name will be generated.
 * @param subdirectory The subdirectory within the system's temporary directory where the file will be created.
 *                     If null, the file will be created directly in the temporary directory.
 * @return The created [File] instance representing the temporary file.
 */
suspend fun createTempFile(
    filename: String? = null,
    subdirectory: String? = null,
    dispatcher: CoroutineDispatcher = Dispatchers.IO
): File = withContext(dispatcher) {
    val tempDir = System.getProperty(TMPDIR_PROPERTY)
    val dirPath = subdirectory?.let { File(tempDir, it) } ?: File(tempDir)
    dirPath.mkdirs()
    filename?.let {
        File(dirPath, it)
    } ?: run {
        createTempFile(dirPath.toPath()).toFile()
    }.apply {
        deleteOnExit()
    }
}

/**
 * Distributes a total of 100 percentage points among the specified parts using the Largest Remainder Method.
 *
 * @param parts The parts to distribute percentage points among.
 * @return A list of integers representing the percentage distribution.
 */
fun percentageDistribution(parts: DoubleArray): List<Int> {
    val total = parts.sum()
    return if (total == 0.0) {
        List(parts.size) { 0 }
    } else {
        val percentages = parts.map { (it / total) * 100.0 }.toTypedArray()
        val intPercentages = percentages.map { it.toInt() }.toTypedArray()
        (100 - intPercentages.sum()).takeIf { it > 0 }?.let { difference ->
            percentages.mapIndexed { index, it -> it % 1.0 to index }
                .sortedByDescending { it.first }
                .take(difference)
                .forEach { (_, index) -> intPercentages[index]++ }
        }
        intPercentages.toList()
    }
}
