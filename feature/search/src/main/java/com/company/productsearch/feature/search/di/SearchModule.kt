package com.company.productsearch.feature.search.di

import com.company.productsearch.core.common.config.AppConfig
import com.company.productsearch.core.domain.usecase.SearchProductsUseCase
import com.company.productsearch.feature.search.ui.SearchViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val searchModule = module {
    single { SearchViewModel(get<SearchProductsUseCase>(), get<AppConfig>()) }
}
