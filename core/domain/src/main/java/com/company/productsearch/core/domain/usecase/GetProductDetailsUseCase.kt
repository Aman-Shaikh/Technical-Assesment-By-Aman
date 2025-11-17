package com.company.productsearch.core.domain.usecase

import com.company.productsearch.core.common.config.AppConfig
import com.company.productsearch.core.domain.model.ProductDetails
import com.company.productsearch.core.domain.repository.ProductRepository

class GetProductDetailsUseCase(
    private val repository: ProductRepository,
    private val appConfig: AppConfig
) {
    suspend operator fun invoke(
        productId: String,
        lang: String? = null
    ): Result<ProductDetails> {
        if (productId.isBlank()) {
            return Result.failure(IllegalArgumentException("Product ID cannot be empty"))
        }
        return repository.getProductDetails(productId, lang ?: appConfig.defaultLanguage)
    }
}

