package com.company.productsearch.core.domain.usecase

import com.company.productsearch.core.common.config.AppConfig
import com.company.productsearch.core.domain.model.Product
import com.company.productsearch.core.domain.repository.ProductRepository

class SearchProductsUseCase(
    private val repository: ProductRepository,
    private val appConfig: AppConfig
) {
    suspend operator fun invoke(
        query: String,
        lang: String? = null,
        page: Int? = null,
        pageSize: Int? = null
    ): Result<List<Product>> {
        if (query.isBlank()) {
            return Result.failure(IllegalArgumentException("Query cannot be empty"))
        }
        return repository.searchProducts(
            query = query,
            lang = lang ?: appConfig.defaultLanguage,
            page = page ?: appConfig.defaultPage,
            pageSize = pageSize ?: appConfig.defaultPageSize
        )
    }
}

