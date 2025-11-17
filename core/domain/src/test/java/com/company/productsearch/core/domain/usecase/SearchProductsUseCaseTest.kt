package com.company.productsearch.core.domain.usecase

import com.company.productsearch.core.common.config.AppConfig
import com.company.productsearch.core.domain.model.Product
import com.company.productsearch.core.domain.repository.ProductRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SearchProductsUseCaseTest {

    private lateinit var productRepository: ProductRepository
    private lateinit var appConfig: AppConfig
    private lateinit var searchProductsUseCase: SearchProductsUseCase

    @Before
    fun setUp() {
        productRepository = mockk()
        appConfig = mockk()
        searchProductsUseCase = SearchProductsUseCase(productRepository, appConfig)
    }

    @Test
    fun `invoke with blank query returns failure`() = runTest {
        val result = searchProductsUseCase(" ")

        assertTrue(result.isFailure)
        assertEquals("Query cannot be empty", result.exceptionOrNull()?.message)
    }

    @Test
    fun `invoke with valid query returns success`() = runTest {
        val products = listOf(Product("1", "Product 1", 10.0, "USD", "url"))
        coEvery { productRepository.searchProducts(any(), any(), any(), any()) } returns Result.success(products)
        coEvery { appConfig.defaultLanguage } returns "en"
        coEvery { appConfig.defaultPage } returns 1
        coEvery { appConfig.defaultPageSize } returns 10

        val result = searchProductsUseCase("product")

        assertTrue(result.isSuccess)
        assertEquals(products, result.getOrNull())
    }
}