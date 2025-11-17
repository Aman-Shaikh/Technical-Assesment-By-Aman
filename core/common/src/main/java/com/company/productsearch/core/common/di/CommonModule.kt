package com.company.productsearch.core.common.di

import com.company.productsearch.core.common.config.AppConfig
import com.company.productsearch.core.common.config.AppConfigImpl
import com.company.productsearch.core.common.network.HttpClientFactory
import org.koin.dsl.module

// Configuration module
val configModule = module {
    single<AppConfig> { AppConfigImpl() }
}

// Network module - Ktor HttpClient configuration
val networkModule = module {
    single { HttpClientFactory() }
    single { get<HttpClientFactory>().create() }
}
