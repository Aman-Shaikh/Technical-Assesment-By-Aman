package com.company.productsearch.core.domain.di

import com.company.productsearch.core.domain.usecase.GetProductDetailsUseCase
import com.company.productsearch.core.domain.usecase.SearchProductsUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val domainModule = module {
    factoryOf(::SearchProductsUseCase)
    factoryOf(::GetProductDetailsUseCase)
}
