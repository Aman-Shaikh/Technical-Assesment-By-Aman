package com.company.productsearch.core.domain.model

data class ProductSpec(
    val group: String? = null,
    val name: String? = null,
    val value: String? = null
)

data class ProductWarranty(
    val parentSku: String? = null,
    val sku: String? = null,
    val title: String? = null,
    val type: String? = null,
    val subType: String? = null,
    val termMonths: Int? = null,
    val regularPrice: Double? = null
)

data class ProductAvailability(
    val sku: String? = null,
    val inStoreAvailability: String? = null,
    val inStoreAvailabilityText: String? = null,
    val isAvailableOnline: Boolean = false,
    val onlineAvailability: String? = null,
    val onlineAvailabilityText: String? = null,
    val onlineAvailabilityCount: Int? = null,
    val buttonState: String? = null
)

data class ProductMedia(
    val thumbnailUrl: String? = null,
    val url: String? = null,
    val mimeType: String? = null
)

data class ProductDetails(
    val id: String, // sku
    val name: String,
    val shortDescription: String? = null,
    val longDescription: String? = null,
    val regularPrice: Double? = null,
    val salePrice: Double? = null,
    val saleStartDate: String? = null,
    val saleEndDate: String? = null,
    val thumbnailImage: String? = null,
    val highResImage: String? = null,
    val additionalMedia: List<ProductMedia> = emptyList(),
    val brandName: String? = null,
    val brandThumbnailImage: String? = null,
    val categoryName: String? = null,
    val customerRating: Double? = null,
    val customerRatingCount: Int? = null,
    val customerReviewCount: Int? = null,
    val availability: ProductAvailability? = null,
    val specs: List<ProductSpec> = emptyList(),
    val warranties: List<ProductWarranty> = emptyList(),
    val whatsInTheBox: List<String> = emptyList(),
    val modelNumber: String? = null,
    val manufacturer: String? = null,
    val upcNumber: String? = null,
    val productUrl: String? = null,
    val seoText: String? = null,
    val hasFreeShipping: Boolean = false,
    val hasHomeDeliveryService: Boolean = false,
    val hasInStorePickup: Boolean = false,
    val isProductOnSale: Boolean = false,
    val isPurchasable: Boolean = false,
    val warrantyAndRepairDisclosureUrl: String? = null
)

