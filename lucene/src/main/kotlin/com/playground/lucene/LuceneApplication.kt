package com.playground.lucene

import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.document.Document
import org.apache.lucene.document.Field
import org.apache.lucene.document.SortedDocValuesField
import org.apache.lucene.document.TextField
import org.apache.lucene.index.DirectoryReader
import org.apache.lucene.index.IndexWriter
import org.apache.lucene.index.IndexWriterConfig
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.search.MatchAllDocsQuery
import org.apache.lucene.search.TopDocs
import org.apache.lucene.search.grouping.GroupingSearch
import org.apache.lucene.search.grouping.TopGroups
import org.apache.lucene.store.Directory
import org.apache.lucene.store.FSDirectory
import org.apache.lucene.util.BytesRef
import java.io.Closeable
import java.nio.file.Paths
import kotlin.random.Random
import kotlin.system.measureTimeMillis


fun main(args: Array<String>) {
    val indexPath = Paths.get("lucene/src/main/resources/index")
    val directory = FSDirectory.open(indexPath)

    val indexingTime = LuceneIndexer(directory, StandardAnalyzer()).use {
        measureTimeMillis {
            it.index("test", Random.nextInt(1000).toString())
            it.optimize()
        }
    }
    println("IndexingTime: $indexingTime ms.")

    val searchingTime = LuceneSearcher(directory).use {
        measureTimeMillis {
            it.search("token", "test")
            it.searchGrouping()
        }
    }

    println("SearchingTime: $searchingTime ms")
}

class LuceneIndexer(private val indexDirectory: Directory, private val analyzer: StandardAnalyzer) : Closeable {

    private val indexWriterConfig = IndexWriterConfig(analyzer)
    private val writer = IndexWriter(indexDirectory, indexWriterConfig)

    fun index(token: String, window: String) {
        val document = Document()

        document.add(SortedDocValuesField("token", BytesRef(token)))
        document.add(TextField("window", window, Field.Store.YES))

        writer.addDocument(document)
    }

    fun optimize() {
        writer.forceMerge(1)
    }

    override fun close() {
        writer.close()
    }
}

class LuceneSearcher(private val indexDirectory: Directory) : Closeable {

    private val indexReader = DirectoryReader.open(indexDirectory)
    private val searcher = IndexSearcher(indexReader)

    fun search(inField: String, queryString: String) {
        val query = SortedDocValuesField.newSlowExactQuery(inField, BytesRef(queryString))
        var topDocs: TopDocs = searcher.search(query, 10)
        val totalHits = topDocs.totalHits.value
        println(String.format("Found %d hits.", totalHits))
        while (topDocs.scoreDocs.isNotEmpty()) {
            val results = topDocs.scoreDocs
            for (scoreDoc in results) {

                //Returns the id of the document matching the query
                val docId = scoreDoc.doc
                val score = scoreDoc.score

                //We fetch the complete document from index via its id
                val document: Document = searcher.doc(docId)

                //Now we print the title of the document
                println(String.format("Found: %s", document["window"]))
            }

            //we fetch the last doc of this page. We will need to pass this to index searcher to get next page.
            val lastDoc = results[results.size - 1]

            //Get next 10 documents after lastDoc. This gets us the next page of search results.
            topDocs = searcher.searchAfter(lastDoc, query, 10)
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

