package com.playground.opennlp

import opennlp.tools.lemmatizer.DictionaryLemmatizer
import opennlp.tools.postag.POSModel
import opennlp.tools.postag.POSTaggerME
import opennlp.tools.tokenize.SimpleTokenizer
import kotlin.system.measureTimeMillis

fun main() {
    val currentThread = Thread.currentThread()
    println("[${currentThread.name}] Started")
    val tokenizer = SimpleTokenizer.INSTANCE
    val tokens = tokenizer.tokenize("John has a sister named Penny.")
    val inputStreamPOSTagger = currentThread.contextClassLoader.getResourceAsStream("./en-pos-maxent.bin")
    val posModel = POSModel(inputStreamPOSTagger)
    val posTagger = POSTaggerME(posModel)
    val tags = posTagger.tag(tokens)
    val dictLemmatizer = currentThread.contextClassLoader.getResourceAsStream("./en-lemmatizer.dict")
    val lemmatizer = DictionaryLemmatizer(dictLemmatizer)
    val lemmas: Array<String>
    val time = measureTimeMillis {
        lemmas = lemmatizer.lemmatize(tokens, tags)
    }
    println(lemmas.contentToString())
    println("Execution time: $time")
}
