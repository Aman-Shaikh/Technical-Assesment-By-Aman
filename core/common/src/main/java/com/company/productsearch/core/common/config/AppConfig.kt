package com.company.productsearch.core.common.config

/**
 * Application configuration interface for managing app-wide settings.
 * This helps avoid hardcoding values throughout the application.
 */
interface AppConfig {
    val baseUrl: String
    val defaultLanguage: String
    val defaultPage: Int
    val defaultPageSize: Int
}

class AppConfigImpl : AppConfig {
    override val baseUrl: String = "http://www.bestbuy.ca/api/v2/json"
    override val defaultLanguage: String = "en"
    override val defaultPage: Int = 1
    override val defaultPageSize: Int = 24
}

