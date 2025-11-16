package com.company.productsearch.feature.productdetail.di

import com.company.productsearch.feature.productdetail.ui.ProductDetailViewModel
import org.koin.dsl.module

val productDetailModule = module {
    single { ProductDetailViewModel(get()) }
}

