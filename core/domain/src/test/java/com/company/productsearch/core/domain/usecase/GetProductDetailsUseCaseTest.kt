package com.company.productsearch.core.domain.usecase

import com.company.productsearch.core.common.config.AppConfig
import com.company.productsearch.core.domain.model.ProductDetails
import com.company.productsearch.core.domain.repository.ProductRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GetProductDetailsUseCaseTest {

    private lateinit var productRepository: ProductRepository
    private lateinit var appConfig: AppConfig
    private lateinit var getProductDetailsUseCase: GetProductDetailsUseCase

    @Before
    fun setUp() {
        productRepository = mockk()
        appConfig = mockk()
        getProductDetailsUseCase = GetProductDetailsUseCase(productRepository, appConfig)
    }

    @Test
    fun `invoke with blank product ID returns failure`() = runTest {
        val result = getProductDetailsUseCase(" ")

        assertTrue(result.isSuccess)
        assertEquals("Product ID cannot be empty", result.exceptionOrNull()?.message)
    }

    @Test
    fun `invoke with valid product ID returns success`() = runTest {
        val productDetails = mockk<ProductDetails>()
        coEvery { productRepository.getProductDetails(any(), any()) } returns Result.success(productDetails)
        coEvery { appConfig.defaultLanguage } returns "en"

        val result = getProductDetailsUseCase("123")

        assertTrue(result.isSuccess)
        assertEquals(productDetails, result.getOrNull())
    }
}