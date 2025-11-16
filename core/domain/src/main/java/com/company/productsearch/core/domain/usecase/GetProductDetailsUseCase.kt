package com.company.productsearch.core.domain.usecase

import com.company.productsearch.core.domain.model.ProductDetails
import com.company.productsearch.core.domain.repository.ProductRepository

class GetProductDetailsUseCase(
    private val repository: ProductRepository
) {
    suspend operator fun invoke(
        productId: String,
        lang: String = "en"
    ): Result<ProductDetails> {
        if (productId.isBlank()) {
            return Result.failure(IllegalArgumentException("Product ID cannot be empty"))
        }
        return repository.getProductDetails(productId, lang)
    }
}

