package com.company.productsearch.feature.productdetail.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.company.productsearch.core.common.config.AppConfig
import com.company.productsearch.core.domain.model.ProductDetails
import com.company.productsearch.core.domain.usecase.GetProductDetailsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProductDetailUiState(
    val productDetails: ProductDetails? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

class ProductDetailViewModel(
    private val getProductDetailsUseCase: GetProductDetailsUseCase,
    private val appConfig: AppConfig
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ProductDetailUiState())
    val uiState: StateFlow<ProductDetailUiState> = _uiState.asStateFlow()
    
    fun loadProductDetails(productId: String, lang: String? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null
            )
            
            getProductDetailsUseCase(productId, lang ?: appConfig.defaultLanguage)
                .onSuccess { productDetails ->
                    _uiState.value = _uiState.value.copy(
                        productDetails = productDetails,
                        isLoading = false
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message
                    )
                }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

