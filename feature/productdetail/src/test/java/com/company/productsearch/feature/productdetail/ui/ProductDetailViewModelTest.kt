package com.company.productsearch.feature.productdetail.ui

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.company.productsearch.core.common.config.AppConfig
import com.company.productsearch.core.domain.model.ProductDetails
import com.company.productsearch.core.domain.usecase.GetProductDetailsUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class ProductDetailViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var getProductDetailsUseCase: GetProductDetailsUseCase
    private lateinit var appConfig: AppConfig
    private lateinit var viewModel: ProductDetailViewModel

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        getProductDetailsUseCase = mockk()
        appConfig = mockk()
        viewModel = ProductDetailViewModel(getProductDetailsUseCase, appConfig)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has null product details, not loading, and no error`() {
        val initialState = viewModel.uiState.value
        assertNull(initialState.productDetails)
        assertFalse(initialState.isLoading)
        assertNull(initialState.error)
    }

    @Test
    fun `loadProductDetails with success updates state correctly`() = runTest {
        // Given
        val productId = "123"
        val productDetails = ProductDetails(
            id = productId,
            name = "Test Product",
            regularPrice = 99.99,
            salePrice = 79.99
        )
        coEvery { getProductDetailsUseCase(productId, any()) } returns Result.success(productDetails)
        coEvery { appConfig.defaultLanguage } returns "en"

        // When
        viewModel.loadProductDetails(productId)

        // Then
        val state = viewModel.uiState.value
        assertEquals(productDetails, state.productDetails)
        assertFalse(state.isLoading)
        assertNull(state.error)
    }

    @Test
    fun `loadProductDetails with failure updates error state`() = runTest {
        // Given
        val productId = "123"
        val errorMessage = "Network error"
        val exception = RuntimeException(errorMessage)
        coEvery { getProductDetailsUseCase(productId, any()) } returns Result.failure(exception)
        coEvery { appConfig.defaultLanguage } returns "en"

        // When
        viewModel.loadProductDetails(productId)

        // Then
        val state = viewModel.uiState.value
        assertNull(state.productDetails)
        assertFalse(state.isLoading)
        assertEquals(errorMessage, state.error)
    }

    @Test
    fun `loadProductDetails sets loading state to true initially`() = runTest {
        // Given
        val productId = "123"
        val productDetails = ProductDetails(
            id = productId,
            name = "Test Product"
        )
        coEvery { getProductDetailsUseCase(productId, any()) } returns Result.success(productDetails)
        coEvery { appConfig.defaultLanguage } returns "en"

        // When
        viewModel.loadProductDetails(productId)

        // Then - loading should be false after completion
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `loadProductDetails with custom language uses provided language`() = runTest {
        // Given
        val productId = "123"
        val customLang = "fr"
        val productDetails = ProductDetails(
            id = productId,
            name = "Produit de Test"
        )
        coEvery { getProductDetailsUseCase(productId, customLang) } returns Result.success(productDetails)

        // When
        viewModel.loadProductDetails(productId, customLang)

        // Then
        val state = viewModel.uiState.value
        assertEquals(productDetails, state.productDetails)
    }

    @Test
    fun `loadProductDetails with null language uses default language from config`() = runTest {
        // Given
        val productId = "123"
        val defaultLang = "es"
        val productDetails = ProductDetails(
            id = productId,
            name = "Producto de Prueba"
        )
        coEvery { appConfig.defaultLanguage } returns defaultLang
        coEvery { getProductDetailsUseCase(productId, defaultLang) } returns Result.success(productDetails)

        // When
        viewModel.loadProductDetails(productId, null)

        // Then
        val state = viewModel.uiState.value
        assertEquals(productDetails, state.productDetails)
    }

    @Test
    fun `loadProductDetails clears previous error before loading`() = runTest {
        // Given
        val productId = "123"
        val invalidProductId = "invalid"
        val initialError = "Previous error"
        coEvery { getProductDetailsUseCase(invalidProductId, any()) } returns Result.failure(
            RuntimeException(initialError)
        )
        coEvery { appConfig.defaultLanguage } returns "en"
        
        // Set initial error state
        viewModel.loadProductDetails(invalidProductId)
        assertEquals(initialError, viewModel.uiState.value.error)
        
        val productDetails = ProductDetails(
            id = productId,
            name = "Test Product"
        )
        coEvery { getProductDetailsUseCase(productId, any()) } returns Result.success(productDetails)

        // When
        viewModel.loadProductDetails(productId)

        // Then
        val state = viewModel.uiState.value
        assertNull(state.error)
        assertEquals(productDetails, state.productDetails)
    }

    @Test
    fun `clearError removes error from state`() = runTest {
        // Given
        val productId = "123"
        val errorMessage = "Test error"
        val exception = RuntimeException(errorMessage)
        coEvery { getProductDetailsUseCase(productId, any()) } returns Result.failure(exception)
        coEvery { appConfig.defaultLanguage } returns "en"
        
        viewModel.loadProductDetails(productId)
        assertEquals(errorMessage, viewModel.uiState.value.error)

        // When
        viewModel.clearError()

        // Then
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `loadProductDetails multiple times updates state correctly`() = runTest {
        // Given
        val productId1 = "123"
        val productId2 = "456"
        val productDetails1 = ProductDetails(
            id = productId1,
            name = "Product 1"
        )
        val productDetails2 = ProductDetails(
            id = productId2,
            name = "Product 2"
        )
        coEvery { getProductDetailsUseCase(productId1, any()) } returns Result.success(productDetails1)
        coEvery { getProductDetailsUseCase(productId2, any()) } returns Result.success(productDetails2)
        coEvery { appConfig.defaultLanguage } returns "en"

        // When
        viewModel.loadProductDetails(productId1)
        val state1 = viewModel.uiState.value
        
        viewModel.loadProductDetails(productId2)
        val state2 = viewModel.uiState.value

        // Then
        assertEquals(productDetails1, state1.productDetails)
        assertEquals(productDetails2, state2.productDetails)
    }
}

