package com.playground.lucene

import java.io.File

fun getDocuments(): List<String> = listOf(
    "This is the first document.",
    "This document is the second document.",
    "And this is the third one.",
    "Is this the first document?",
)

fun loadDocuments(filePath: String, count: Int? = null): Sequence<String> = File(filePath)
    .bufferedReader()
    .lineSequence()
    .withIndex()
    .takeWhile { (index, _) -> count?.let { index < it } ?: true }
    .map { (_, value) -> value.split("\t")[1] }
