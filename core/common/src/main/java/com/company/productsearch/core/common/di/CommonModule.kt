package com.company.productsearch.core.common.di

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.dsl.module

// Network module - Ktor HttpClient configuration
val networkModule = module {
    single {
        HttpClient(CIO) {
            // JSON Content Negotiation
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                        prettyPrint = true
                        isLenient = true
                        encodeDefaults = false
                    }
                )
            }
            
            // Logging (useful for debugging)
            install(Logging) {
                logger = Logger.DEFAULT
                level = LogLevel.ALL
            }
            

            // Engine configuration
            engine {
                requestTimeout = 30000 // 30 seconds
            }
        }
    }
}
