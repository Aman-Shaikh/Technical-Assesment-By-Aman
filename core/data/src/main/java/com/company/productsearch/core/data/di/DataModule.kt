package com.company.productsearch.core.data.di

import com.company.productsearch.core.data.remote.ProductApi
import com.company.productsearch.core.data.remote.ProductApiImpl
import com.company.productsearch.core.data.repository.ProductRepositoryImpl
import com.company.productsearch.core.domain.repository.ProductRepository
import org.koin.dsl.module

val dataModule = module {
    single<ProductApi> { ProductApiImpl(get(), get()) }
    single<ProductRepository> { ProductRepositoryImpl(get(), get()) }
}
