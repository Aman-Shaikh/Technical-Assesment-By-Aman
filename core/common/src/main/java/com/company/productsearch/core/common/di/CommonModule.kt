package com.company.productsearch.core.common.di

import com.company.productsearch.core.common.config.AppConfig
import com.company.productsearch.core.common.config.AppConfigImpl
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.dsl.module

// Configuration module
val configModule = module {
    single<AppConfig> { AppConfigImpl() }
}

// Network module - Ktor HttpClient configuration
val networkModule = module {
    single {
        HttpClient(OkHttp) {
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
                config {
                    followRedirects(true)
                    // TODO Add SSL Pinning
                }
            }
        }
    }
}
