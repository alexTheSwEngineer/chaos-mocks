package com.alext.chasomocks.chaosmocks

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ChaosMocksApplication

fun main(args: Array<String>) {
	runApplication<ChaosMocksApplication>(*args)
}
