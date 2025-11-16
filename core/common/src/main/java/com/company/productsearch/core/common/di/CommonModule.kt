package com.company.productsearch.core.common.di

import org.koin.dsl.module

// Network module - will be configured when Ktor is added
val networkModule = module {
    // HttpClient will be added here when Ktor is implemented
    // single {
    //     HttpClient(CIO) {
    //         install(ContentNegotiation) {
    //             json(Json {
    //                 ignoreUnknownKeys = true
    //                 prettyPrint = true
    //             })
    //         }
    //     }
    // }
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

