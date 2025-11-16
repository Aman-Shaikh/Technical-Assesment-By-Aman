package com.company.productsearch.core.domain.usecase

import com.company.productsearch.core.domain.model.Product
import com.company.productsearch.core.domain.repository.ProductRepository

class SearchProductsUseCase(
    private val repository: ProductRepository
) {
    suspend operator fun invoke(
        query: String,
        lang: String = "en",
        page: Int = 1,
        pageSize: Int = 24
    ): Result<List<Product>> {
        if (query.isBlank()) {
            return Result.failure(IllegalArgumentException("Query cannot be empty"))
        }
        return repository.searchProducts(query, lang, page, pageSize)
    }
}

