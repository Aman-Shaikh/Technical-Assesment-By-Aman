# Android Product Search App - Architecture Document

## 1. Summary

A modern Android product search application built with Kotlin, Jetpack Compose, and Clean Architecture principles. The app implements a single-activity architecture with feature-based modules, supporting product search, result listing with pagination, and detailed product views. The architecture emphasizes testability, offline support, performance optimization, and production-ready concerns including dependency injection, reactive data flows, caching strategies, and comprehensive error handling.

---

## 2. Module & Package Layout

```
app/
├── build.gradle.kts
└── src/main/java/com/company/productsearch/
    ├── ProductSearchApplication.kt
    └── di/AppModule.kt // Koin module

core/
├── common/
│   └── src/main/java/com/company/productsearch/core/common/
│       ├── di/CommonModule.kt // Koin module for network, db
│       ├── ui/Theme.kt
│       ├── ui/components/
│       ├── utils/Result.kt
│       └── utils/Extensions.kt
├── data/
│   └── src/main/java/com/company/productsearch/core/data/
│       ├── local/
│       │   ├── ProductDao.kt
│       │   ├── SearchQueryDao.kt
│       │   └── ProductDatabase.kt
│       ├── remote/
│       │   ├── ProductApi.kt // Ktor API service
│       │   ├── ProductDto.kt
│       │   └── SearchResponseDto.kt
│       └── repository/
│           └── ProductRepositoryImpl.kt
└── domain/
    └── src/main/java/com/company/productsearch/core/domain/
        ├── model/Product.kt
        ├── repository/ProductRepository.kt
        └── usecase/
            ├── SearchProductsUseCase.kt
            └── GetProductDetailsUseCase.kt

feature/
├── search/
│   └── src/main/java/com/company/productsearch/feature/search/
│       ├── di/SearchModule.kt // Koin module
│       ├── ui/SearchScreen.kt
│       ├── ui/SearchViewModel.kt
│       └── ui/components/ProductItem.kt
└── productdetail/
    └── src/main/java/com/company/productsearch/feature/productdetail/
        ├── di/ProductDetailModule.kt // Koin module
        ├── ui/ProductDetailScreen.kt
        └── ui/ProductDetailViewModel.kt
```

---

## 3. Module Responsibilities

### app
- Application entry point
- Dependency injection root (Koin initialization in Application class)
- MainActivity with Navigation Compose setup
- App-level theme and configuration

### core:common
- Shared utilities and extensions
- Network configuration (Ktor, CIO engine, plugins)
- Database setup (Room)
- Common UI components (loading states, error states, empty states)
- Result wrapper for error handling
- Koin modules for shared dependencies (network, database)

### core:data
- Data layer implementation
- Remote data sources (Ktor API services, DTOs, mappers)
- Local data sources (Room DAOs, entities)
- Repository implementations
- Data mapping (DTO → Domain model)
- Caching logic and sync strategies

### core:domain
- Business logic layer
- Domain models (pure Kotlin data classes)
- Repository interfaces
- Use cases (business logic orchestration)
- No Android dependencies

### feature:search
- Search feature UI and ViewModel
- Search input handling
- Product list display with pagination
- Navigation to product detail
- Feature-specific Koin modules

### feature:productdetail
- Product detail UI and ViewModel
- Product information display
- Add to cart functionality
- Feature-specific Koin modules

---

## 4. Key Interfaces & Sample Signatures

### Domain Models

```kotlin
// core/domain/src/main/java/com/company/productsearch/core/domain/model/Product.kt
data class Product(
    val id: String,
    val title: String,
    val price: Double,
    val currency: String,
    val imageUrl: String,
    val description: String
)
```

### Repository Interface

```kotlin
// core/domain/src/main/java/com/company/productsearch/core/domain/repository/ProductRepository.kt
interface ProductRepository {
    fun searchProducts(
        query: String,
        page: Int = 1,
        pageSize: Int = 20
    ): Flow<PagingData<Product>>
    
    suspend fun getProductById(id: String): Result<Product>
    
    suspend fun refreshSearchResults(query: String): Result<Unit>
}
```

### Use Cases

```kotlin
// core/domain/src/main/java/com/company/productsearch/core/domain/usecase/SearchProductsUseCase.kt
class SearchProductsUseCase(
    private val repository: ProductRepository
) {
    operator fun invoke(
        query: String,
        pageSize: Int = 20
    ): Flow<PagingData<Product>> {
        return repository.searchProducts(query, pageSize = pageSize)
            .cachedIn(viewModelScope)
    }
}

// core/domain/src/main/java/com/company/productsearch/core/domain/usecase/GetProductDetailsUseCase.kt
class GetProductDetailsUseCase(
    private val repository: ProductRepository
) {
    suspend operator fun invoke(productId: String): Result<Product> {
        return repository.getProductById(productId)
    }
}
```

### ViewModel

```kotlin
// feature/search/src/main/java/com/company/productsearch/feature/search/ui/SearchViewModel.kt
// Injected via Koin
class SearchViewModel(
    private val searchProductsUseCase: SearchProductsUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    val products: Flow<PagingData<Product>> = searchQuery
        .debounce(300)
        .filter { it.length >= 2 }
        .flatMapLatest { query ->
            searchProductsUseCase(query)
        }
        .cachedIn(viewModelScope)
    
    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }
    
    fun onProductClicked(productId: String) {
        // Navigate to detail
    }
}

data class SearchUiState(
    val isLoading: Boolean = false,
    val error: String? = null
)
```

### Repository Implementation

```kotlin
// core/data/src/main/java/com/company/productsearch/core/data/repository/ProductRepositoryImpl.kt
class ProductRepositoryImpl(
    private val remoteDataSource: ProductRemoteDataSource,
    private val localDataSource: ProductLocalDataSource,
    private val mapper: ProductMapper
) : ProductRepository {
    
    override fun searchProducts(
        query: String,
        page: Int,
        pageSize: Int
    ): Flow<PagingData<Product>> {
        return Pager(
            config = PagingConfig(pageSize = pageSize, enablePlaceholders = false),
            remoteMediator = ProductRemoteMediator(query, remoteDataSource, localDataSource),
            pagingSourceFactory = { localDataSource.searchProducts(query) }
        ).flow.map { pagingData ->
            pagingData.map { entity -> mapper.toDomain(entity) }
        }
    }
    
    override suspend fun getProductById(id: String): Result<Product> {
        return try {
            // Try cache first
            localDataSource.getProductById(id)?.let { entity ->
                return Result.success(mapper.toDomain(entity))
            }
            
            // Fallback to network
            val dto = remoteDataSource.getProductById(id)
            val product = mapper.toDomain(dto)
            
            // Cache for offline
            localDataSource.insertProduct(mapper.toEntity(dto))
            
            Result.success(product)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

### DTOs (with Kotlinx.Serialization)

```kotlin
// core/data/src/main/java/com/company/productsearch/core/data/remote/ProductDto.kt
@Serializable
data class ProductDto(
    @SerialName("id") val id: String,
    @SerialName("title") val title: String,
    @SerialName("price") val price: Double,
    @SerialName("currency") val currency: String,
    @SerialName("image_url") val imageUrl: String,
    @SerialName("description") val description: String
)

@Serializable
data class SearchResponseDto(
    @SerialName("products") val products: List<ProductDto>,
    @SerialName("total") val total: Int,
    @SerialName("page") val page: Int,
    @SerialName("page_size") val pageSize: Int
)
```

### Dependency Injection (Koin)

```kotlin
// app/src/main/java/com/company/productsearch/di/AppModule.kt
val appModule = module {
    // ViewModel for Search feature
    viewModel { SearchViewModel(get()) }
    
    // ViewModel for Product Detail feature
    viewModel { ProductDetailViewModel(get()) }
}

// core/common/src/main/java/com/company/productsearch/di/CommonModule.kt
val networkModule = module {
    single {
        HttpClient(CIO) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    prettyPrint = true
                })
            }
            install(HttpRequestRetry) {
                // ... retry config
            }
        }
    }
}

val dataModule = module {
    single<ProductRepository> { ProductRepositoryImpl(get(), get(), get()) }
    single<ProductRemoteDataSource> { ProductRemoteDataSourceImpl(get()) }
    // ... other data sources, DAOs, mappers
}

val domainModule = module {
    factory { SearchProductsUseCase(get()) }
    factory { GetProductDetailsUseCase(get()) }
}

// In Application class
class ProductSearchApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@ProductSearchApplication)
            modules(appModule, networkModule, dataModule, domainModule)
        }
    }
}
```

---

## 5. Data Flow Sequence & Threading

### Search Flow

```
User Input → ViewModel.onSearchQueryChanged()
    ↓
Debounce (300ms) on Main thread
    ↓
Filter (min 2 chars)
    ↓
SearchProductsUseCase.invoke()
    ↓
ProductRepository.searchProducts()
    ↓
Pager with RemoteMediator
    ↓
[RemoteMediator] Check local cache → Load from DB (IO thread)
    ↓
If cache stale/missing → RemoteDataSource.fetch() (IO thread)
    ↓
Map DTO → Domain model (IO thread)
    ↓
Insert into Room DB (IO thread)
    ↓
PagingData emitted to Flow
    ↓
ViewModel collects → Update UI (Main thread via collectAsLazyPagingItems)
    ↓
Compose recomposition → Display products
```

### Threading Model

- **Main Thread**: UI updates, Compose recomposition, ViewModel state changes
- **IO Thread (Dispatchers.IO)**: Network calls, database operations, DTO mapping
- **Default Thread (Dispatchers.Default)**: CPU-intensive operations (if any)

### Backpressure Handling

- **Flow**: Uses `flatMapLatest` to cancel previous searches when new query arrives
- **Paging 3**: Built-in backpressure handling via `PagingData`
- **Debounce**: Prevents excessive API calls during typing
- **CachedIn**: Caches PagingData in ViewModel scope to prevent re-fetching

---

## 6. Caching & Sync Strategy

### Multi-Layer Caching

1. **In-Memory Cache (L1)**
   - ViewModel state holds current search results
   - PagingData cached via `cachedIn(viewModelScope)`
   - Lifecycle: Cleared when ViewModel cleared

2. **Database Cache (L2 - Room)**
   - Products stored in Room with search query association
   - Search queries cached separately with timestamp
   - Lifecycle: Persistent across app restarts
   - Stale threshold: 5 minutes for search results, 1 hour for product details

3. **Network (L3)**
   - API as source of truth
   - Used when cache miss or stale

### Stale-While-Revalidate Strategy

```kotlin
// RemoteMediator implementation pattern
class ProductRemoteMediator {
    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, ProductEntity>
    ): MediatorResult {
        return try {
            val cacheTime = getCacheTimestamp(query)
            val isStale = System.currentTimeMillis() - cacheTime > STALE_THRESHOLD
            
            // Return cached data immediately if available
            val cachedData = localDataSource.getCachedResults(query)
            
            // Fetch fresh data in background if stale
            if (isStale || loadType == LoadType.REFRESH) {
                val freshData = remoteDataSource.fetch(query, page)
                localDataSource.insertAll(freshData)
                localDataSource.updateCacheTimestamp(query)
            }
            
            MediatorResult.Success(endOfPaginationReached = false)
        } catch (e: Exception) {
            // Return cached data even on error
            if (cachedData.isNotEmpty()) {
                MediatorResult.Success(endOfPaginationReached = true)
            } else {
                MediatorResult.Error(e)
            }
        }
    }
}
```

### Pagination Behavior

- **Initial Load**: 20 items
- **Page Size**: 20 items per page
- **Prefetch Distance**: 5 items before end of list
- **Placeholders**: Disabled for simplicity
- **Remote Mediator**: Handles network pagination and local caching

---

## 7. Error Handling, Retry & Offline Patterns

### Result Wrapper

```kotlin
// core/common/src/main/java/com/company/productsearch/core/common/utils/Result.kt
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val exception: Throwable) : Result<Nothing>()
    object Loading : Result<Nothing>()
}
```

### Error Types

```kotlin
sealed class AppError : Exception() {
    object NetworkError : AppError()
    object NotFoundError : AppError()
    object ServerError : AppError()
    data class UnknownError(val cause: Throwable) : AppError()
}
```

### Retry Policy (Ktor)

```kotlin
// Ktor HttpClient plugin for retry with exponential backoff
val httpClient = HttpClient(CIO) {
    install(HttpRequestRetry) {
        retryOnServerErrors(maxRetries = 3)
        exponentialDelay()
        modifyRequest { request ->
            request.headers.append("X-RETRY-COUNT", retryCount.toString())
        }
    }
}
```

### UI Error Handling

```kotlin
// ViewModel error handling
sealed class UiState<out T> {
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String, val retry: () -> Unit) : UiState<Nothing>()
    object Loading : UiState<Nothing>()
    object Empty : UiState<Nothing>()
}

// One-shot side effects for errors
private val _uiEvent = Channel<UiEvent>()
val uiEvent = _uiEvent.receiveAsFlow()

sealed class UiEvent {
    data class ShowError(val message: String) : UiEvent()
    data class NavigateToDetail(val productId: String) : UiEvent()
}

// In Compose
LaunchedEffect(Unit) {
    viewModel.uiEvent.collect { event ->
        when (event) {
            is UiEvent.ShowError -> {
                scaffoldState.snackbarHostState.showSnackbar(event.message)
            }
            is UiEvent.NavigateToDetail -> {
                navController.navigate("product_detail/${event.productId}")
            }
        }
    }
}
```

### Offline Support

- **Read Operations**: Always try cache first, show cached data immediately
- **Write Operations**: Queue for sync when online (if needed in future)
- **Network Status**: Monitor connectivity via `ConnectivityManager` or `NetworkCallback`
- **UI Indication**: Show offline indicator when network unavailable
- **Graceful Degradation**: Show cached results with "Offline" badge

---

## 8. Testing Strategy & Examples

### Testing Matrix

| Component | Test Type | Framework | Coverage Target |
|-----------|-----------|-----------|-----------------|
| Use Cases | Unit | JUnit 5, MockK | 90%+ |
| ViewModels | Unit | JUnit 5, MockK, Turbine, Koin Test | 85%+ |
| Repositories | Integration | JUnit 5, Room in-memory DB, Koin Test | 80%+ |
| Mappers | Unit | JUnit 5 | 95%+ |
| UI Components | UI | Compose Test, Espresso | 70%+ |
| Navigation | Integration | Navigation Testing | 80%+ |

### Sample Unit Test - Use Case

```kotlin
// core/domain/src/test/java/com/company/productsearch/core/domain/usecase/SearchProductsUseCaseTest.kt
@ExtendWith(MockKExtension::class)
class SearchProductsUseCaseTest {

    @MockK
    private lateinit var repository: ProductRepository

    private lateinit var useCase: SearchProductsUseCase

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        useCase = SearchProductsUseCase(repository)
    }

    @Test
    fun `invoke should return flow of paging data from repository`() = runTest {
        // Given
        val query = "laptop"
        val expectedProducts = listOf(
            Product("1", "Laptop", 999.99, "USD", "url", "desc")
        )
        val pagingData = PagingData.from(expectedProducts)

        every { repository.searchProducts(query, pageSize = 20) } returns flowOf(pagingData)

        // When
        val result = use
    }
}
```