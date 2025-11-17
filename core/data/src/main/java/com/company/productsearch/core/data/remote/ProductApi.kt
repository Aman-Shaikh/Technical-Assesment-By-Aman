package com.company.productsearch.core.data.remote

import com.company.productsearch.core.common.config.AppConfig
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

interface ProductApi {
    suspend fun searchProducts(
        query: String,
        lang: String,
        page: Int,
        pageSize: Int
    ): SearchResponseDto
    
    suspend fun getProductDetails(
        productId: String,
        lang: String
    ): ProductDetailsDto
}

class ProductApiImpl(
    private val httpClient: HttpClient,
    private val appConfig: AppConfig
) : ProductApi {
    
    override suspend fun searchProducts(
        query: String,
        lang: String,
        page: Int,
        pageSize: Int
    ): SearchResponseDto {
        return httpClient.get("${appConfig.baseUrl}/search") {
            parameter("lang", lang)
            parameter("query", query)
            parameter("page", page)
            parameter("pageSize", pageSize)
        }.body()
    }
    
    override suspend fun getProductDetails(
        productId: String,
        lang: String
    ): ProductDetailsDto {
        return httpClient.get("${appConfig.baseUrl}/product/$productId") {
            parameter("lang", lang)
        }.body()
    }
}

