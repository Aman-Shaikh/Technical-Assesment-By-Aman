package com.company.productsearch.feature.search.ui

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.company.productsearch.core.common.config.AppConfig
import com.company.productsearch.core.domain.model.Product
import com.company.productsearch.core.domain.usecase.SearchProductsUseCase

internal class SearchPagingSource(
    private val query: String,
    private val searchProductsUseCase: SearchProductsUseCase,
    private val appConfig: AppConfig
) : PagingSource<Int, Product>() {

    override fun getRefreshKey(state: PagingState<Int, Product>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Product> {
        val page = params.key ?: appConfig.defaultPage
        val result = searchProductsUseCase(
            query = query,
            page = page,
            pageSize = appConfig.defaultPageSize
        )
        return result.fold(
            onSuccess = { products ->
                LoadResult.Page(
                    data = products,
                    prevKey = if (page == appConfig.defaultPage) null else page - 1,
                    nextKey = if (products.size < appConfig.defaultPageSize) null else page + 1
                )
            },
            onFailure = { throwable ->
                LoadResult.Error(throwable)
            }
        )
    }
}

