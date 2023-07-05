package com.playground.lucene

import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.document.Document
import org.apache.lucene.document.Field
import org.apache.lucene.document.SortedDocValuesField
import org.apache.lucene.document.TextField
import org.apache.lucene.store.FSDirectory
import org.apache.lucene.util.BytesRef
import java.nio.file.Paths
import kotlin.random.Random
import kotlin.system.measureTimeMillis

fun main(args: Array<String>) {
    val indexPath = Paths.get("lucene/src/main/resources/index")
    val directory = FSDirectory.open(indexPath)

    val indexingTime = Indexer(directory, StandardAnalyzer()).use {
        measureTimeMillis {
            it.index(createWindowedToken("test", Random.nextInt(1000).toString()))
            it.optimize()
        }
    }
    println("IndexingTime: $indexingTime ms.")

    val searchingTime = Searcher(directory).use {
        measureTimeMillis {
            it.search("token", "test")
            it.searchGrouping()
        }
    }

    println("SearchingTime: $searchingTime ms")
}

private fun createWindowedToken(token: String, window: String): Document {
    val document = Document()

    document.add(SortedDocValuesField("token", BytesRef(token)))
    document.add(TextField("window", window, Field.Store.YES))

    return document
}
