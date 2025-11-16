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

@Serializable
data class AdditionalMediaDto(
    @SerialName("thumbnailUrl") val thumbnailUrl: String? = null,
    @SerialName("url") val url: String? = null,
    @SerialName("mimeType") val mimeType: String? = null
)

@Serializable
data class AvailabilityDto(
    @SerialName("sku") val sku: String? = null,
    @SerialName("inStoreAvailability") val inStoreAvailability: String? = null,
    @SerialName("inStoreAvailabilityText") val inStoreAvailabilityText: String? = null,
    @SerialName("inStoreAvailabilityUpdateDate") val inStoreAvailabilityUpdateDate: String? = null,
    @SerialName("isAvailableOnline") val isAvailableOnline: Boolean = false,
    @SerialName("onlineAvailability") val onlineAvailability: String? = null,
    @SerialName("onlineAvailabilityText") val onlineAvailabilityText: String? = null,
    @SerialName("onlineAvailabilityUpdateDate") val onlineAvailabilityUpdateDate: String? = null,
    @SerialName("onlineAvailabilityCount") val onlineAvailabilityCount: Int? = null,
    @SerialName("onlineAvailabilityZoneCount") val onlineAvailabilityZoneCount: Int? = null,
    @SerialName("buttonState") val buttonState: String? = null
)

@Serializable
data class SpecDto(
    @SerialName("group") val group: String? = null,
    @SerialName("name") val name: String? = null,
    @SerialName("value") val value: String? = null
)

@Serializable
data class WarrantyDto(
    @SerialName("parentSku") val parentSku: String? = null,
    @SerialName("sku") val sku: String? = null,
    @SerialName("title") val title: String? = null,
    @SerialName("type") val type: String? = null,
    @SerialName("subType") val subType: String? = null,
    @SerialName("termMonths") val termMonths: Int? = null,
    @SerialName("regularPrice") val regularPrice: Double? = null
)

@Serializable
data class ProductDetailsDto(
    @SerialName("additionalMedia") val additionalMedia: List<AdditionalMediaDto>? = null,
    @SerialName("altLangSeoText") val altLangSeoText: String? = null,
    @SerialName("availability") val availability: AvailabilityDto? = null,
    @SerialName("brandName") val brandName: String? = null,
    @SerialName("brandThumbnailImage") val brandThumbnailImage: String? = null,
    @SerialName("categoryName") val categoryName: String? = null,
    @SerialName("customerRating") val customerRating: Double? = null,
    @SerialName("customerRatingCount") val customerRatingCount: Int? = null,
    @SerialName("customerReviewCount") val customerReviewCount: Int? = null,
    @SerialName("hasFreeShipping") val hasFreeShipping: Boolean = false,
    @SerialName("hasFrenchContent") val hasFrenchContent: Boolean = false,
    @SerialName("hasHomeDeliveryService") val hasHomeDeliveryService: Boolean = false,
    @SerialName("hasInStorePickup") val hasInStorePickup: Boolean = false,
    @SerialName("hasPromotion") val hasPromotion: Boolean = false,
    @SerialName("hasRebate") val hasRebate: Boolean = false,
    @SerialName("hasWarranty") val hasWarranty: Boolean = false,
    @SerialName("highResImage") val highResImage: String? = null,
    @SerialName("isAdvertised") val isAdvertised: Boolean = false,
    @SerialName("isAvailableForOrder") val isAvailableForOrder: Boolean = false,
    @SerialName("isAvailableForPickup") val isAvailableForPickup: Boolean = false,
    @SerialName("isBackorderable") val isBackorderable: Boolean = false,
    @SerialName("isClearance") val isClearance: Boolean = false,
    @SerialName("isFrenchCompliant") val isFrenchCompliant: Boolean = false,
    @SerialName("isInStoreOnly") val isInStoreOnly: Boolean = false,
    @SerialName("isMachineTranslated") val isMachineTranslated: Boolean = false,
    @SerialName("isMarketplace") val isMarketplace: Boolean = false,
    @SerialName("isOnlineOnly") val isOnlineOnly: Boolean = false,
    @SerialName("isPreorderable") val isPreorderable: Boolean = false,
    @SerialName("isPriceEndsLabel") val isPriceEndsLabel: Boolean = false,
    @SerialName("isProductOnSale") val isProductOnSale: Boolean = false,
    @SerialName("isPurchasable") val isPurchasable: Boolean = false,
    @SerialName("isShippable") val isShippable: Boolean = false,
    @SerialName("isSpecialDelivery") val isSpecialDelivery: Boolean = false,
    @SerialName("isVisible") val isVisible: Boolean = true,
    @SerialName("longDescription") val longDescription: String? = null,
    @SerialName("make") val make: String? = null,
    @SerialName("manufacturer") val manufacturer: String? = null,
    @SerialName("modelNumber") val modelNumber: String? = null,
    @SerialName("name") val name: String,
    @SerialName("offerId") val offerId: String? = null,
    @SerialName("regularPrice") val regularPrice: Double? = null,
    @SerialName("salePrice") val salePrice: Double? = null,
    @SerialName("saleStartDate") val saleStartDate: String? = null,
    @SerialName("saleEndDate") val saleEndDate: String? = null,
    @SerialName("shortDescription") val shortDescription: String? = null,
    @SerialName("sku") val sku: String,
    @SerialName("specs") val specs: List<SpecDto>? = null,
    @SerialName("thumbnailImage") val thumbnailImage: String? = null,
    @SerialName("upcNumber") val upcNumber: String? = null,
    @SerialName("warranties") val warranties: List<WarrantyDto>? = null,
    @SerialName("whatsInTheBox") val whatsInTheBox: List<String>? = null,
    @SerialName("productUrl") val productUrl: String? = null,
    @SerialName("seoText") val seoText: String? = null,
    @SerialName("warrantyAndRepairDisclosureUrl") val warrantyAndRepairDisclosureUrl: String? = null
)

