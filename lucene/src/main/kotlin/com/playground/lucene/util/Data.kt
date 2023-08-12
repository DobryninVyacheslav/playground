package com.playground.lucene.util

import java.io.File

fun getTextDocuments(): List<String> = listOf(
    "This is the first document.",
    "This document is the second document.",
    "And this is the third one.",
    "Is this the first document?",
)

fun loadDocuments(filePath: String, count: Int? = null): Sequence<Document> = File(filePath)
    .bufferedReader()
    .lineSequence()
    .withIndex()
    .takeWhile { (index, _) -> count?.let { index < it } ?: true }
    .map { (_, value) ->
        val (id, content) = value.split("\t")
        Document(id, content)
    }

data class Document(val id: String, val content: String)

fun prepareQrelsAndQueriesForCollection(
    collectionPath: String,
    qrelsPath: String,
    qrelsOutPath: String,
    queriesPath: String,
    queriesOutPath: String,
    firstDocumentNumber: Int = 500_000,
) {
    // get docIds for firstDocumentNumber documents
    val docIds: Set<Int> = File(collectionPath).bufferedReader()
        .lineSequence()
        .take(firstDocumentNumber)
        .mapTo(mutableSetOf()) { it.split("\t").first().toInt() }
    // filter qrels by docIds
    val qrelsLines = File(qrelsPath).bufferedReader()
        .lineSequence()
        .filterTo(mutableSetOf()) { it.split("\t")[2].toInt() in docIds }
    // save qrels
    File(qrelsOutPath).bufferedWriter().use { file -> qrelsLines.forEach { file.appendLine(it) } }
    // filter queries by qrels
    val qIds = qrelsLines.map { it.split("\t").first() }
        .mapTo(mutableSetOf()) { it.toInt() }
    val queriesLines = File(queriesPath).bufferedReader()
        .lineSequence()
        .filterTo(mutableSetOf()) { it.split("\t").first().toInt() in qIds }
    // save queries
    File(queriesOutPath).bufferedWriter().use { file -> queriesLines.forEach { file.appendLine(it) } }
}
