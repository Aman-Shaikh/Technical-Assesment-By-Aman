package com.company.productsearch.feature.search.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.company.productsearch.core.domain.model.Product
import com.company.productsearch.core.domain.usecase.SearchProductsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

data class SearchUiState(
    val query: String = "",
    val products: List<Product> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val currentPage: Int = 1,
    val totalPages: Int = 1,
    val hasMore: Boolean = false
)

class SearchViewModel(
    private val searchProductsUseCase: SearchProductsUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()
    
    private val _searchQuery = MutableStateFlow("")
    
    init {
        // Debounce search queries
        _searchQuery
            .debounce(500) // 500ms debounce
            .filter { it.length >= 2 } // Minimum 2 characters
            .onEach { query ->
                if (query.isNotBlank()) {
                    searchProducts(query)
                }
            }
            .launchIn(viewModelScope)
    }
    
    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        _uiState.value = _uiState.value.copy(query = query)
    }
    
    fun searchProducts(query: String, page: Int = 1) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null
            )
            
            searchProductsUseCase(query, "en", page, 24)
                .onSuccess { products ->
                    _uiState.value = _uiState.value.copy(
                        products = if (page == 1) products else _uiState.value.products + products,
                        isLoading = false,
                        currentPage = page,
                        hasMore = products.isNotEmpty() && products.size >= 24
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "An error occurred"
                    )
                }
        }
    }
    
    fun loadMore() {
        val currentState = _uiState.value
        if (!currentState.isLoading && currentState.hasMore) {
            searchProducts(currentState.query, currentState.currentPage + 1)
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    fun onProductClicked(productId: String) {
        // Navigation will be handled by the UI
    }
}

