package com.playground.lucene

import java.io.BufferedWriter
import java.io.File

fun writeToFile(path: String, action: (BufferedWriter) -> Unit) = File(path)
    .bufferedWriter()
    .use { file -> action(file) }
