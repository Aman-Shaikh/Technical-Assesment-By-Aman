package com.company.productsearch.feature.search.di

import com.company.productsearch.feature.search.ui.SearchViewModel
import org.koin.dsl.module

val searchModule = module {
    single { SearchViewModel(get()) }
}
