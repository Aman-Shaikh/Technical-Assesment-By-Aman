package com.company.productsearch.feature.search.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import org.koin.androidx.compose.koinViewModel
import com.company.productsearch.feature.search.ui.components.ProductItem
import com.company.productsearch.feature.search.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: SearchViewModel = koinViewModel(),
    onProductClick: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val listState = rememberLazyListState()
    val keyboardController = LocalSoftwareKeyboardController.current
    val defaultErrorMessage = stringResource(R.string.error_occurred)
    val products = viewModel.products.collectAsLazyPagingItems()
    val query = uiState.query.trim()
    val isQueryShort = query.length < 2
    val hasActiveSearch = uiState.lastSearchedQuery != null
    val isRefreshing = products.loadState.refresh is LoadState.Loading
    val isInitialLoading = hasActiveSearch && isRefreshing && products.itemCount == 0
    val isAppending = hasActiveSearch && products.loadState.append is LoadState.Loading
    val searchEnabled = !isQueryShort
    
    LaunchedEffect(products.loadState) {
        val errorState = products.loadState.refresh as? LoadState.Error
            ?: products.loadState.append as? LoadState.Error
            ?: products.loadState.prepend as? LoadState.Error
        errorState?.let { error ->
            val message = error.error.message?.takeIf { it.isNotBlank() } ?: defaultErrorMessage
            snackbarHostState.showSnackbar(message)
        }
    }
    
    LaunchedEffect(uiState.lastSearchedQuery) {
        if (uiState.lastSearchedQuery != null) {
            listState.scrollToItem(0)
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.product_search)) }
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Enhanced Search Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = uiState.query,
                    onValueChange = viewModel::onSearchQueryChanged,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    placeholder = { 
                        Text(
                            text = stringResource(R.string.search_products_hint),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        ) 
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    ),
                    textStyle = MaterialTheme.typography.bodyLarge,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        imeAction = ImeAction.Search
                    ),
                    keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                        onSearch = {
                            if (!isQueryShort) {
                                viewModel.performSearch()
                                keyboardController?.hide()
                            }
                        }
                    )
                )
                
                IconButton(
                    onClick = {
                        if (searchEnabled) {
                            viewModel.performSearch()
                            keyboardController?.hide()
                        }
                    },
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            color = if (searchEnabled) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            }
                        ),
                    enabled = searchEnabled
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = stringResource(R.string.search_button_content_description),
                        tint = if (searchEnabled) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        },
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Content
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                when {
                    isInitialLoading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    isQueryShort && !hasActiveSearch -> {
                        Text(
                            text = stringResource(R.string.enter_at_least_2_characters),
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.Center)
                                .padding(16.dp)
                        )
                    }
                    !hasActiveSearch -> {
                        Text(
                            text = stringResource(R.string.search_products_hint),
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.Center)
                                .padding(16.dp)
                        )
                    }
                    hasActiveSearch && products.itemCount == 0 && products.loadState.refresh is LoadState.NotLoading -> {
                        Text(
                            text = stringResource(R.string.no_products_found),
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.Center)
                                .padding(16.dp)
                        )
                    }
                    else -> {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
                        ) {
                            items(
                                count = products.itemCount,
                                key = { index -> products[index]?.id ?: index }
                            ) { index ->
                                val product = products[index]
                                if (product != null) {
                                    ProductItem(
                                        product = product,
                                        onClick = { productId ->
                                            onProductClick(productId)
                                            viewModel.onProductClicked(productId)
                                        }
                                    )
                                } else {
                                    Spacer(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(80.dp)
                                    )
                                }
                            }

                            if (isAppending) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator()
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

