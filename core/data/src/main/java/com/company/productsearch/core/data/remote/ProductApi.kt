package com.company.productsearch.core.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

interface ProductApi {
    suspend fun searchProducts(
        query: String,
        lang: String = "en",
        page: Int = 1,
        pageSize: Int = 24
    ): SearchResponseDto
}

class ProductApiImpl(
    private val httpClient: HttpClient,
    private val baseUrl: String = "http://www.bestbuy.ca/api/v2/json"
) : ProductApi {
    
    override suspend fun searchProducts(
        query: String,
        lang: String,
        page: Int,
        pageSize: Int
    ): SearchResponseDto {
        return httpClient.get("$baseUrl/search") {
            parameter("lang", lang)
            parameter("query", query)
            parameter("page", page)
            parameter("pageSize", pageSize)
        }.body()
    }
}

