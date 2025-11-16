package com.company.productsearch.core.data.repository

import com.company.productsearch.core.data.remote.ProductApi
import com.company.productsearch.core.data.remote.toDomain
import com.company.productsearch.core.domain.model.Product
import com.company.productsearch.core.domain.model.ProductDetails
import com.company.productsearch.core.domain.repository.ProductRepository

class ProductRepositoryImpl(
    private val productApi: ProductApi
) : ProductRepository {
    
    override suspend fun searchProducts(
        query: String,
        lang: String,
        page: Int,
        pageSize: Int
    ): Result<List<Product>> {
        return try {
            val response = productApi.searchProducts(query, lang, page, pageSize)
            val products = response.products.map { it.toDomain() }
            Result.success(products)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getProductById(id: String): Result<Product> {
        return try {
            val response = productApi.searchProducts(id, pageSize = 1)
            val product = response.products.firstOrNull()?.toDomain()
                ?: return Result.failure(IllegalArgumentException("Product not found"))
            Result.success(product)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getProductDetails(
        productId: String,
        lang: String
    ): Result<ProductDetails> {
        return try {
            val response = productApi.getProductDetails(productId, lang)
            Result.success(response.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

