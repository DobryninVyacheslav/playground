package com.playground.lucene.util

import org.apache.lucene.analysis.Analyzer
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute

private val regex = Regex("[^А-Яа-яA-Za-z0-9 ]")
fun cleanText(text: String): String = text.replace(regex, " ")

fun extractTotalBow(texts: Collection<String>): Map<String, Int> =
    texts.flatMapTo(mutableListOf()) { text -> cleanText(text).lowercase().split(" ") }
        .groupingBy { it }
        .eachCount()

fun Analyzer.analyze(text: String): List<String> {
    val tokens = mutableListOf<String>()
    val tokenStream = this.tokenStream(null, text)
    val attr = tokenStream.addAttribute(CharTermAttribute::class.java)
    tokenStream.reset()
    while (tokenStream.incrementToken()) {
        tokens.add(attr.toString())
    }
    tokenStream.end()
    tokenStream.close()
    return tokens
}
