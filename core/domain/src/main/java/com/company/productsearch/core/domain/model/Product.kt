package com.company.productsearch.core.domain.model

data class Product(
    val id: String, // sku
    val title: String, // name
    val price: Double, // salePrice or regularPrice
    val currency: String = "CAD",
    val imageUrl: String, // thumbnailImage or highResImage
    val description: String? = null, // shortDescription
    val rating: Double? = null, // customerRating
    val ratingCount: Int? = null, // customerRatingCount
    val regularPrice: Double? = null,
    val salePrice: Double? = null,
    val categoryName: String? = null,
    val productUrl: String? = null
)

