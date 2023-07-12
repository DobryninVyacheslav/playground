package com.playground.spring.web.controller

import com.playground.spring.web.dto.NameDto
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/")
class HelloController {

    @GetMapping("")
    fun getHelloWorld() = "Hello world!"

    @GetMapping("/greet")
    fun getGreet() = "Hey!"

    @PostMapping("/greet")
    fun getGreetWithName(@RequestBody body: NameDto) = "Hello, ${body.name}"
}
