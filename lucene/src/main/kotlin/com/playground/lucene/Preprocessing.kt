package com.playground.lucene

private val regex = Regex("[^А-Яа-яA-Za-z0-9 ]")
fun cleanText(text: String): String = text.replace(regex, " ")

fun extractTotalBow(texts: Collection<String>): Map<String, Int> =
    texts.flatMapTo(mutableListOf()) { text -> cleanText(text).lowercase().split(" ") }
        .groupingBy { it }
        .eachCount()
