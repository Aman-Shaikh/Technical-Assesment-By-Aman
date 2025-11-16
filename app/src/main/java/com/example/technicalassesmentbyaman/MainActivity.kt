package com.example.technicalassesmentbyaman

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.company.productsearch.feature.productdetail.ui.ProductDetailScreen
import com.company.productsearch.feature.search.ui.SearchScreen
import com.example.technicalassesmentbyaman.ui.theme.TechnicalAssesmentByAmanTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TechnicalAssesmentByAmanTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var selectedProductId by remember { mutableStateOf<String?>(null) }
                    
                    when {
                        selectedProductId != null -> {
                            ProductDetailScreen(
                                productId = selectedProductId!!,
                                onBackClick = { selectedProductId = null }
                            )
                        }
                        else -> {
                            SearchScreen(
                                onProductClick = { productId ->
                                    selectedProductId = productId
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}