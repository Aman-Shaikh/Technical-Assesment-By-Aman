package com.company.productsearch

import android.app.Application
import com.company.productsearch.di.appModule
import com.company.productsearch.core.common.di.networkModule
import com.company.productsearch.core.common.di.dataModule
import com.company.productsearch.core.common.di.domainModule
import com.company.productsearch.feature.search.di.searchModule
import com.company.productsearch.feature.productdetail.di.productDetailModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class ProductSearchApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@ProductSearchApplication)
            modules(
                appModule,
                networkModule,
                dataModule,
                domainModule,
                searchModule,
                productDetailModule
            )
        }
    }
}

