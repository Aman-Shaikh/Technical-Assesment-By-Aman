package com.company.productsearch.core.common.di

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.observer.ResponseObserver
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
                level = LogLevel.INFO
            }
            
            // Response observer for monitoring
            install(ResponseObserver) {
                onResponse { response ->
                    // Can add response monitoring/logging here
                }
            }
            
            // Engine configuration
            engine {
                // Configure CIO engine settings if needed
                requestTimeout = 30000 // 30 seconds
            }
        }
    }
}

// Data module - will be configured when repositories are implemented
val dataModule = module {
    // Repositories and data sources will be added here
    // single<ProductRepository> { ProductRepositoryImpl(get(), get(), get()) }
    // single<ProductRemoteDataSource> { ProductRemoteDataSourceImpl(get()) }
}

// Domain module - will be configured when use cases are implemented
val domainModule = module {
    // Use cases will be added here
    // factory { SearchProductsUseCase(get()) }
    // factory { GetProductDetailsUseCase(get()) }
}

