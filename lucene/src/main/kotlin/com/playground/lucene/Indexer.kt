package com.playground.lucene

import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.document.Document
import org.apache.lucene.index.IndexWriter
import org.apache.lucene.index.IndexWriterConfig
import org.apache.lucene.store.Directory
import java.io.Closeable

class Indexer(indexDirectory: Directory, analyzer: StandardAnalyzer) : Closeable {

    private val indexWriterConfig = IndexWriterConfig(analyzer)
    private val writer = IndexWriter(indexDirectory, indexWriterConfig)

    fun index(document: Document) {
        writer.addDocument(document)
    }

    fun optimize() {
        writer.forceMerge(1)
    }

    override fun close() {
        writer.close()
    }
}
