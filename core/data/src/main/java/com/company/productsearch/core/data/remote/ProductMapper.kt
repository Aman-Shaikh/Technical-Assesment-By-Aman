package com.company.productsearch.core.data.remote

import com.company.productsearch.core.domain.model.Product

fun ProductDto.toDomain(): Product {
    val price = salePrice ?: regularPrice ?: 0.0
    val imageUrl = highResImage ?: thumbnailImage ?: ""
    
    return Product(
        id = sku,
        title = name,
        price = price,
        currency = "CAD",
        imageUrl = imageUrl,
        description = shortDescription,
        rating = customerRating,
        ratingCount = customerRatingCount,
        regularPrice = regularPrice,
        salePrice = salePrice,
        categoryName = categoryName,
        productUrl = productUrl
    )
}

