package pl.piasta.newswise.common

import kotlin.properties.ReadOnlyProperty
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Provides a delegated property for obtaining a [logger][Logger] instance using the [LoggerFactory].
 * This logger can be used for logging messages related to the class where it is utilized.
 *
 * Usage example within a companion object:
 * ```
 * class MyClass {
 *     companion object {
 *         private val logger by log()
 *     }
 *
 *     fun doSomething() {
 *         logger.info("Doing something...")
 *     }
 * }
 * ```
 *
 * @return A delegated property that provides a [logger][Logger] instance.
 */
fun log() = ReadOnlyProperty<Any, Logger> { thisRef, _ ->
    if (thisRef::class.isCompanion) {
        LoggerFactory.getLogger(thisRef.javaClass.enclosingClass)
    } else LoggerFactory.getLogger(thisRef.javaClass)
}
