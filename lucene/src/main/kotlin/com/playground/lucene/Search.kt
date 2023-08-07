package com.playground.lucene

import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.search.Query
import org.apache.lucene.search.ScoreDoc

fun IndexSearcher.searchAll(query: Query): Sequence<ScoreDoc> {
    val pageSize = 1000
    var topDocs = this.search(query, pageSize)
    println("Found ${topDocs.totalHits.value} hits")
    return sequence {
        while (topDocs.scoreDocs.isNotEmpty()) {
            for (scoreDoc in topDocs.scoreDocs) {
                yield(scoreDoc)
            }
            val lastScoreDoc = topDocs.scoreDocs.last()
            topDocs = this@searchAll.searchAfter(lastScoreDoc, query, pageSize)
        }
    }
}
