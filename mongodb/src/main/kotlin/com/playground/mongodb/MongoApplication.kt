package com.playground.mongodb

import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.mongodb.core.MongoTemplate

@SpringBootApplication
class MongoApplication(private val mongoTemplate: MongoTemplate) : CommandLineRunner {
    /**
     * Play here
     */
    override fun run(vararg args: String) {
        val result = mongoTemplate.collectionNames
        println(result)
    }
}

fun main(args: Array<String>) {
    runApplication<MongoApplication>(*args)
}
