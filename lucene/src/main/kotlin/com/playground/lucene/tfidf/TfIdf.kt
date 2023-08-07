package com.playground.lucene.tfidf

import com.playground.lucene.Indexer
import com.playground.lucene.Searcher
import com.playground.lucene.analyze
import com.playground.lucene.loadDocuments
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.document.Document
import org.apache.lucene.document.Field
import org.apache.lucene.document.FieldType
import org.apache.lucene.index.IndexOptions
import org.apache.lucene.index.Term
import org.apache.lucene.search.MatchAllDocsQuery
import org.apache.lucene.store.Directory
import org.apache.lucene.store.FSDirectory
import org.apache.lucene.util.BytesRef
import java.nio.file.Paths
import kotlin.system.measureTimeMillis

private const val FIELD_NAME = "content"
private const val MAX_SCORE = 15.438689
private var currentMaxScore = Float.MIN_VALUE
private const val THRESHOLD_VALUE = 0.1
private val analyzer = StandardAnalyzer()

fun main(args: Array<String>) {
    val documents = loadDocuments(args.first())
    // val documents = getDocuments()
    // val vocabulary = extractTotalBow(documents)
    // println("Vocabulary size: $vocabulary")

    val indexPath = Paths.get("lucene/index")
    val directory = FSDirectory.open(indexPath)

    // directory.index(documents)
    val searchTime = measureTimeMillis { directory.search() }

    println("MAX_SCORE=$MAX_SCORE, currentMaxScore=$currentMaxScore")
    println("Search time: $searchTime ms")
    // vocabulary.keys
    //     .asSequence()
    //     .filter { it.isNotBlank() }
    //     .map {
    //         it to runCatching { directory.search(it) }.getOrElse { ex ->
    //             println("Error: ${ex.message}")
    //             null
    //         }
    //     }
    //     .filter { it.second != null }
    //     .sortedByDescending { it.second }
    //     .forEach{}
}

fun Directory.index(documents: Sequence<String>): Unit = Indexer(this, StandardAnalyzer()) {
    // similarity = ClassicSimilarity()
}.use { indexer ->
    documents.forEach { content ->
        val document = Document().apply {
            val fieldType = FieldType()
            fieldType.setStored(true)
            fieldType.setIndexOptions(IndexOptions.DOCS)
            fieldType.setStoreTermVectors(true)
            add(Field(FIELD_NAME, content, fieldType))
        }
        indexer.index(document)
    }
    indexer.optimize()
}

fun Directory.search() = Searcher(this).use { searcher ->
    searcher.search { reader ->
        // this.similarity = ClassicSimilarity()
        searcher.searchAll(MatchAllDocsQuery()).forEach { scoreDoc ->
            val content = storedFields().document(scoreDoc.doc)[FIELD_NAME]
            val iterator = reader.termVectors()[scoreDoc.doc, FIELD_NAME].iterator()
            val belowThresholdTokens = mutableSetOf<String>()
            var bytesRef: BytesRef?
            val joinedTerms = StringBuilder()
            while (iterator.next().also { bytesRef = it } != null) {
                val term = Term(FIELD_NAME, bytesRef)
                val termFreq = iterator.totalTermFreq()
                val docCount = iterator.docFreq()
                val score = similarity.scorer(
                    1f,
                    collectionStatistics(FIELD_NAME),
                    termStatistics(term, reader.docFreq(term), reader.totalTermFreq(term))
                ).score(termFreq.toFloat(), 1) // ).score(reader.totalTermFreq(term).toFloat(), 1)
                val normalizedScore = score / MAX_SCORE
                if (normalizedScore < THRESHOLD_VALUE) {
                    belowThresholdTokens += term.text()
                    println("term: ${term.text()}, termFreq=$termFreq, docCount=$docCount, score=$normalizedScore")
                }
                joinedTerms.append(term.text()).append(" ")
                if (score > currentMaxScore) currentMaxScore = score
            }
            val filteredContent = (analyzer.analyze(content) - belowThresholdTokens).joinToString(" ")
            println("content=$content,\njoinedTerms=$joinedTerms,\nfilteredContent=$filteredContent")
            println()
        }
    }
}
