#!/bin/sh

///bin/true <<EOC
/*
EOC
kotlin "$0" "$@"
exit $?
*/

@file:CompilerOptions("-jvm-target", "19", "-Xopt-in=kotlin.RequiresOptIn")

import java.lang.System.getenv
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.bufferedReader

val dataPath: Path = getenv("DATA_PATH")
    ?.let { Path(it).resolve("data.dat") }
    ?: error("DATA_PATH environment variable is not set!")

dataPath.bufferedReader().useLines { lines ->
    lines
        .map { it.split(" ").first() }
        .groupingBy { it }
        .eachCount()
}.forEach { (label, count) -> println("$label: $count") }
