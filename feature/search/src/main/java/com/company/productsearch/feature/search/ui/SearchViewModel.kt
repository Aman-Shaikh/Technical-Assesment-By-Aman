package com.company.productsearch.feature.search.ui

import androidx.compose.foundation.lazy.LazyListState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.company.productsearch.core.common.config.AppConfig
import com.company.productsearch.core.domain.model.Product
import com.company.productsearch.core.domain.usecase.SearchProductsUseCase
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SearchUiState(
    val query: String = "",
    val lastSearchedQuery: String? = null
)

class SearchViewModel(
    private val searchProductsUseCase: SearchProductsUseCase,
    private val appConfig: AppConfig
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()
    var listState: LazyListState = LazyListState()
        private set

    private val _resetScroll = MutableStateFlow(false)
    val resetScroll = _resetScroll.asStateFlow()

    fun updateScrollPosition(index: Int, offset: Int) {
        savedIndex = index
        savedOffset = offset
    }

    var savedIndex = 0
    var savedOffset = 0

    private val _searchRequests = MutableSharedFlow<String>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    internal val searchRequests = _searchRequests.asSharedFlow()

    val products: Flow<PagingData<Product>> = searchRequests
        .onStart { emit("") }
        .flatMapLatest { query ->
            if (query.length < MIN_QUERY_LENGTH) {
                flowOf(PagingData.empty())
            } else {
                Pager(
                    config = PagingConfig(
                        pageSize = appConfig.defaultPageSize,
                        initialLoadSize = appConfig.defaultPageSize,
                        prefetchDistance = 1,
                        enablePlaceholders = false
                    )
                ) {
                    SearchPagingSource(query, searchProductsUseCase, appConfig)
                }.flow
            }
        }
        .cachedIn(viewModelScope)

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(query = query) }
        if (query.trim().length < MIN_QUERY_LENGTH && _uiState.value.lastSearchedQuery != null) {
            viewModelScope.launch {
                _uiState.update { it.copy(lastSearchedQuery = null) }
                _searchRequests.emit("")
            }
        }
    }

    fun performSearch() {
        val query = _uiState.value.query.trim()
        if (query.length < MIN_QUERY_LENGTH) return

        _resetScroll.value = true

        viewModelScope.launch {
            _uiState.update { it.copy(lastSearchedQuery = query) }
            _searchRequests.emit(query)
        }
    }

    fun onScrollResetHandled() {
        _resetScroll.value = false
    }

    companion object {
        private const val MIN_QUERY_LENGTH = 2
    }
}

