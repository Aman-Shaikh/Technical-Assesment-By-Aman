package com.company.productsearch.feature.search.ui

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.company.productsearch.core.common.config.AppConfig
import com.company.productsearch.core.domain.usecase.SearchProductsUseCase
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class SearchViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var searchProductsUseCase: SearchProductsUseCase
    private lateinit var appConfig: AppConfig
    private lateinit var viewModel: SearchViewModel

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        searchProductsUseCase = mockk()
        appConfig = mockk {
            every { defaultPage } returns 1
            every { defaultPageSize } returns 8
        }
        viewModel = SearchViewModel(searchProductsUseCase, appConfig)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `onSearchQueryChanged updates query`() {
        val newQuery = "test query"
        viewModel.onSearchQueryChanged(newQuery)
        assertEquals(newQuery, viewModel.uiState.value.query)
    }

    @Test
    fun `performSearch with short query does nothing`() = runTest {
        viewModel.onSearchQueryChanged("a")
        viewModel.performSearch()
        assertNull(viewModel.uiState.value.lastSearchedQuery)
    }

    @Test
    fun `performSearch with valid query stores last search`() = runTest {
        val query = "laptop"
        viewModel.onSearchQueryChanged(query)
        viewModel.performSearch()
        assertEquals(query, viewModel.uiState.value.lastSearchedQuery)
    }
}