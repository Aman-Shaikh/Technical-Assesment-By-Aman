package com.company.productsearch.core.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProductDto(
    @SerialName("sku") val sku: String,
    @SerialName("name") val name: String,
    @SerialName("shortDescription") val shortDescription: String? = null,
    @SerialName("customerRating") val customerRating: Double? = null,
    @SerialName("customerRatingCount") val customerRatingCount: Int? = null,
    @SerialName("customerReviewCount") val customerReviewCount: Int? = null,
    @SerialName("productUrl") val productUrl: String? = null,
    @SerialName("regularPrice") val regularPrice: Double? = null,
    @SerialName("salePrice") val salePrice: Double? = null,
    @SerialName("saleEndDate") val saleEndDate: Long? = null,
    @SerialName("thumbnailImage") val thumbnailImage: String? = null,
    @SerialName("highResImage") val highResImage: String? = null,
    @SerialName("categoryName") val categoryName: String? = null,
    @SerialName("seoText") val seoText: String? = null,
    @SerialName("hasPromotion") val hasPromotion: Boolean = false,
    @SerialName("isAdvertised") val isAdvertised: Boolean = false,
    @SerialName("isClearance") val isClearance: Boolean = false,
    @SerialName("isInStoreOnly") val isInStoreOnly: Boolean = false,
    @SerialName("isOnlineOnly") val isOnlineOnly: Boolean = false,
    @SerialName("isVisible") val isVisible: Boolean = true,
    @SerialName("isPreorderable") val isPreorderable: Boolean = false,
    @SerialName("categoryIds") val categoryIds: List<String>? = null
)

@Serializable
data class SearchResponseDto(
    @SerialName("currentPage") val currentPage: Int,
    @SerialName("total") val total: Int,
    @SerialName("totalPages") val totalPages: Int,
    @SerialName("pageSize") val pageSize: Int,
    @SerialName("products") val products: List<ProductDto>
)

