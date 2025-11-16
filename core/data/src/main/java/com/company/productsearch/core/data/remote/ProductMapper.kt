package com.company.productsearch.core.data.remote

import com.company.productsearch.core.domain.model.Product
import com.company.productsearch.core.domain.model.ProductDetails
import com.company.productsearch.core.domain.model.ProductAvailability
import com.company.productsearch.core.domain.model.ProductMedia
import com.company.productsearch.core.domain.model.ProductSpec
import com.company.productsearch.core.domain.model.ProductWarranty

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

fun AdditionalMediaDto.toDomain(): ProductMedia {
    return ProductMedia(
        thumbnailUrl = thumbnailUrl,
        url = url,
        mimeType = mimeType
    )
}

fun AvailabilityDto.toDomain(): ProductAvailability {
    return ProductAvailability(
        sku = sku,
        inStoreAvailability = inStoreAvailability,
        inStoreAvailabilityText = inStoreAvailabilityText,
        isAvailableOnline = isAvailableOnline,
        onlineAvailability = onlineAvailability,
        onlineAvailabilityText = onlineAvailabilityText,
        onlineAvailabilityCount = onlineAvailabilityCount,
        buttonState = buttonState
    )
}

fun SpecDto.toDomain(): ProductSpec {
    return ProductSpec(
        group = group,
        name = name,
        value = value
    )
}

fun WarrantyDto.toDomain(): ProductWarranty {
    return ProductWarranty(
        parentSku = parentSku,
        sku = sku,
        title = title,
        type = type,
        subType = subType,
        termMonths = termMonths,
        regularPrice = regularPrice
    )
}

fun ProductDetailsDto.toDomain(): ProductDetails {
    return ProductDetails(
        id = sku,
        name = name,
        shortDescription = shortDescription,
        longDescription = longDescription,
        regularPrice = regularPrice,
        salePrice = salePrice,
        saleStartDate = saleStartDate,
        saleEndDate = saleEndDate,
        thumbnailImage = thumbnailImage,
        highResImage = highResImage,
        additionalMedia = additionalMedia?.map { it.toDomain() } ?: emptyList(),
        brandName = brandName,
        brandThumbnailImage = brandThumbnailImage,
        categoryName = categoryName,
        customerRating = customerRating,
        customerRatingCount = customerRatingCount,
        customerReviewCount = customerReviewCount,
        availability = availability?.toDomain(),
        specs = specs?.map { it.toDomain() } ?: emptyList(),
        warranties = warranties?.map { it.toDomain() } ?: emptyList(),
        whatsInTheBox = whatsInTheBox ?: emptyList(),
        modelNumber = modelNumber,
        manufacturer = manufacturer,
        upcNumber = upcNumber,
        productUrl = productUrl,
        seoText = seoText,
        hasFreeShipping = hasFreeShipping,
        hasHomeDeliveryService = hasHomeDeliveryService,
        hasInStorePickup = hasInStorePickup,
        isProductOnSale = isProductOnSale,
        isPurchasable = isPurchasable,
        warrantyAndRepairDisclosureUrl = warrantyAndRepairDisclosureUrl
    )
}

