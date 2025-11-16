package com.company.productsearch.core.domain.repository

import com.company.productsearch.core.domain.model.Product

interface ProductRepository {
    suspend fun searchProducts(
        query: String,
        lang: String = "en",
        page: Int = 1,
        pageSize: Int = 24
    ): Result<List<Product>>
    
    suspend fun getProductById(id: String): Result<Product>
}

