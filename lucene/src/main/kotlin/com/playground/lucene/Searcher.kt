package com.playground.lucene

import org.apache.lucene.document.SortedDocValuesField
import org.apache.lucene.index.DirectoryReader
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.search.MatchAllDocsQuery
import org.apache.lucene.search.Query
import org.apache.lucene.search.ScoreDoc
import org.apache.lucene.search.grouping.GroupingSearch
import org.apache.lucene.search.grouping.TopGroups
import org.apache.lucene.store.Directory
import org.apache.lucene.util.BytesRef
import java.io.Closeable

class Searcher(indexDirectory: Directory) : Closeable {

    private val indexReader = DirectoryReader.open(indexDirectory)
    private val searcher = IndexSearcher(indexReader)

    fun <T> withSearcherAndReader(action: IndexSearcher.(DirectoryReader) -> T): T {
        return searcher.action(indexReader)
    }

    fun searchAll(query: Query): Sequence<ScoreDoc> {
        val pageSize = 1000
        var topDocs = searcher.search(query, pageSize)
        return sequence {
            while (topDocs.scoreDocs.isNotEmpty()) {
                for (scoreDoc in topDocs.scoreDocs) {
                    yield(scoreDoc)
                }
                val lastScoreDoc = topDocs.scoreDocs.last()
                topDocs = searcher.searchAfter(lastScoreDoc, query, pageSize)
            }
        }
    }

    fun search(inField: String, queryString: String) {
        val query = SortedDocValuesField.newSlowExactQuery(inField, BytesRef(queryString))
        val storedFields = searcher.storedFields()
        searchAll(query).forEach {
            val document = storedFields.document(it.doc)
            println(String.format("Found: %s", document["window"]))
        }
    }

    fun searchGrouping() {
        val groupingSearch = GroupingSearch("token")
        groupingSearch.setAllGroups(true)
        groupingSearch.setGroupDocsOffset(0)
        groupingSearch.setGroupDocsLimit(1_000_000)
        val result: TopGroups<BytesRef> = groupingSearch.search(searcher, MatchAllDocsQuery(), 0, 100)
        println("TotalGroupCount: ${result.totalGroupCount}. " +
            "TotalHitCount: ${result.totalHitCount}. " +
            "TotalGroupedHitCount: ${result.totalGroupedHitCount}")
        result.groups.forEach {
            println("Token: ${it.groupValue.utf8ToString()}. " +
                "TotalHits: ${it.totalHits}. " +
                "GroupedCount: ${it.scoreDocs.size}")
            it.scoreDocs.map { scoreDoc -> println(searcher.storedFields().document(scoreDoc.doc)) }
        }
    }

    override fun close() {
        indexReader.close()
    }
}
