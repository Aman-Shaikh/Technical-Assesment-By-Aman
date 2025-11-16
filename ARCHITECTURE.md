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
    └── di/AppModule.kt

core/
├── common/
│   └── src/main/java/com/company/productsearch/core/common/
│       ├── di/CommonModule.kt
│       ├── network/NetworkModule.kt
│       ├── database/DatabaseModule.kt
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
│       │   ├── ProductApi.kt
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
│       ├── di/SearchModule.kt
│       ├── ui/SearchScreen.kt
│       ├── ui/SearchViewModel.kt
│       └── ui/components/ProductItem.kt
└── productdetail/
    └── src/main/java/com/company/productsearch/feature/productdetail/
        ├── di/ProductDetailModule.kt
        ├── ui/ProductDetailScreen.kt
        └── ui/ProductDetailViewModel.kt
```

---

## 3. Module Responsibilities

### app
- Application entry point
- Dependency injection root (Hilt Application class)
- MainActivity with Navigation Compose setup
- App-level theme and configuration

### core:common
- Shared utilities and extensions
- Network configuration (Retrofit, OkHttp, interceptors)
- Database setup (Room)
- Common UI components (loading states, error states, empty states)
- Result wrapper for error handling
- Dependency injection modules for shared dependencies

### core:data
- Data layer implementation
- Remote data sources (API interfaces, DTOs, mappers)
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
- Feature-specific dependency injection

### feature:productdetail
- Product detail UI and ViewModel
- Product information display
- Add to cart functionality
- Feature-specific dependency injection

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
@HiltViewModel
class SearchViewModel @Inject constructor(
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
@Singleton
class ProductRepositoryImpl @Inject constructor(
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

### DTOs

```kotlin
// core/data/src/main/java/com/company/productsearch/core/data/remote/ProductDto.kt
@JsonClass(generateAdapter = true)
data class ProductDto(
    @Json(name = "id") val id: String,
    @Json(name = "title") val title: String,
    @Json(name = "price") val price: Double,
    @Json(name = "currency") val currency: String,
    @Json(name = "image_url") val imageUrl: String,
    @Json(name = "description") val description: String
)

@JsonClass(generateAdapter = true)
data class SearchResponseDto(
    @Json(name = "products") val products: List<ProductDto>,
    @Json(name = "total") val total: Int,
    @Json(name = "page") val page: Int,
    @Json(name = "page_size") val pageSize: Int
)
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

### Retry Policy

```kotlin
// Network interceptor with exponential backoff
class RetryInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        var response: Response? = null
        var exception: IOException? = null
        
        repeat(MAX_RETRIES) { attempt ->
            try {
                response = chain.proceed(request)
                if (response.isSuccessful) return response
            } catch (e: IOException) {
                exception = e
                if (attempt < MAX_RETRIES - 1) {
                    delay(calculateBackoff(attempt))
                }
            }
        }
        
        throw exception ?: IOException("Request failed")
    }
    
    private fun calculateBackoff(attempt: Int): Long {
        return minOf(1000L * (1 shl attempt), 10000L)
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
| ViewModels | Unit | JUnit 5, MockK, Turbine | 85%+ |
| Repositories | Integration | JUnit 5, Room in-memory DB | 80%+ |
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
        val result = useCase(query).first()
        
        // Then
        val items = result.collectDataForTest()
        assertEquals(1, items.size)
        assertEquals("Laptop", items[0].title)
    }
}
```

### Sample ViewModel Test

```kotlin
// feature/search/src/test/java/com/company/productsearch/feature/search/ui/SearchViewModelTest.kt
@ExtendWith(MockKExtension::class)
class SearchViewModelTest {
    
    @MockK
    private lateinit var searchProductsUseCase: SearchProductsUseCase
    
    private lateinit var viewModel: SearchViewModel
    
    @Before
    fun setup() {
        MockKAnnotations.init(this)
        viewModel = SearchViewModel(searchProductsUseCase)
    }
    
    @Test
    fun `onSearchQueryChanged should update search query`() = runTest {
        // Given
        val query = "phone"
        
        // When
        viewModel.onSearchQueryChanged(query)
        
        // Then
        assertEquals(query, viewModel.searchQuery.value)
    }
    
    @Test
    fun `search should debounce rapid input changes`() = runTest {
        // Given
        every { searchProductsUseCase(any()) } returns flowOf(PagingData.empty())
        
        // When
        viewModel.onSearchQueryChanged("p")
        viewModel.onSearchQueryChanged("ph")
        viewModel.onSearchQueryChanged("pho")
        viewModel.onSearchQueryChanged("phon")
        viewModel.onSearchQueryChanged("phone")
        
        // Then
        delay(400) // Wait for debounce
        verify(exactly = 1) { searchProductsUseCase("phone") }
    }
}
```

### Sample UI Test

```kotlin
// feature/search/src/androidTest/java/com/company/productsearch/feature/search/ui/SearchScreenTest.kt
@RunWith(AndroidJUnit4::class)
class SearchScreenTest {
    
    @get:Rule
    val composeTestRule = createComposeRule()
    
    @Test
    fun searchScreen_displaysEmptyState_whenNoQuery() {
        composeTestRule.setContent {
            SearchScreen(
                uiState = SearchUiState(),
                searchQuery = "",
                products = flowOf(PagingData.empty()),
                onSearchQueryChanged = {},
                onProductClicked = {}
            )
        }
        
        composeTestRule.onNodeWithText("Search for products").assertIsDisplayed()
    }
    
    @Test
    fun searchScreen_displaysProducts_whenResultsAvailable() {
        val products = listOf(
            Product("1", "Laptop", 999.99, "USD", "url", "desc")
        )
        
        composeTestRule.setContent {
            SearchScreen(
                uiState = SearchUiState(),
                searchQuery = "laptop",
                products = flowOf(PagingData.from(products)),
                onSearchQueryChanged = {},
                onProductClicked = {}
            )
        }
        
        composeTestRule.onNodeWithText("Laptop").assertIsDisplayed()
        composeTestRule.onNodeWithText("$999.99").assertIsDisplayed()
    }
}
```

---

## 9. CI Checklist & GitHub Actions

### CI Checklist

- [ ] **Linting**: ktlint/Detekt on all modules
- [ ] **Unit Tests**: Run all unit tests (use cases, ViewModels, mappers)
- [ ] **Integration Tests**: Run repository integration tests
- [ ] **UI Tests**: Run Compose UI tests on emulator
- [ ] **Build Matrix**: Test on API 24, 28, 33, 34
- [ ] **Code Coverage**: Enforce minimum 80% coverage
- [ ] **Dependency Check**: Check for vulnerable dependencies
- [ ] **APK Generation**: Build release APK/AAB
- [ ] **Publishing**: Upload to internal testing track (optional)

### GitHub Actions Workflow

```yaml
name: Android CI

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main, develop ]

jobs:
  lint:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-java@v3
        with:
          distribution: 'temurin'
          java-version: '17'
      - name: Run ktlint
        run: ./gradlew ktlintCheck
      - name: Run Detekt
        run: ./gradlew detekt

  unit-tests:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-java@v3
        with:
          distribution: 'temurin'
          java-version: '17'
      - name: Run unit tests
        run: ./gradlew test
      - name: Generate coverage report
        run: ./gradlew jacocoTestReport
      - name: Upload coverage
        uses: codecov/codecov-action@v3

  instrumented-tests:
    runs-on: macos-latest
    strategy:
      matrix:
        api-level: [24, 28, 33, 34]
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-java@v3
        with:
          distribution: 'temurin'
          java-version: '17'
      - name: Setup Android SDK
        uses: android-actions/setup-android@v2
      - name: Run instrumented tests
        uses: reactivecircus/android-emulator-runner@v2
        with:
          api-level: ${{ matrix.api-level }}
          script: ./gradlew connectedAndroidTest

  build:
    runs-on: ubuntu-latest
    needs: [lint, unit-tests]
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-java@v3
        with:
          distribution: 'temurin'
          java-version: '17'
      - name: Build release
        run: ./gradlew assembleRelease bundleRelease
      - name: Upload artifacts
        uses: actions/upload-artifact@v3
        with:
          name: app-release
          path: app/build/outputs/
```

---

## 10. Non-Functional Requirements & Performance/Security

### Performance Checklist

- [ ] **Image Loading**: Coil with memory/disk caching, placeholder/error handling
- [ ] **List Performance**: LazyColumn with proper key() and remember optimizations
- [ ] **Pagination**: Efficient loading with Paging 3, no memory leaks
- [ ] **Database**: Indexed queries, batch operations, background threading
- [ ] **Network**: Request/response caching, connection pooling, compression
- [ ] **Memory**: Leak detection, proper lifecycle management, image size optimization
- [ ] **Startup**: Lazy initialization, background work deferral, app startup metrics

### Security Checklist

- [ ] **Network Security**: HTTPS only, certificate pinning (optional for production)
- [ ] **API Keys**: Stored in local.properties (dev) or secure storage (prod)
- [ ] **Data Storage**: Encrypted SharedPreferences for sensitive data
- [ ] **ProGuard/R8**: Obfuscation enabled, keep rules for reflection
- [ ] **Input Validation**: Sanitize search queries, prevent injection attacks
- [ ] **Deep Links**: Validate and sanitize deep link parameters
- [ ] **Logging**: No sensitive data in logs, use ProGuard to strip logs in release

### Observability

```kotlin
// Logging interceptor for network debugging
class LoggingInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val startTime = System.currentTimeMillis()
        
        logger.d("Request: ${request.method} ${request.url}")
        
        val response = chain.proceed(request)
        val duration = System.currentTimeMillis() - startTime
        
        logger.d("Response: ${response.code} in ${duration}ms")
        
        return response
    }
}

// Analytics events
interface Analytics {
    fun logSearchEvent(query: String, resultCount: Int)
    fun logProductView(productId: String)
    fun logError(error: Throwable, context: String)
}

// Performance monitoring
class PerformanceMonitor {
    fun trackScreenLoad(screenName: String, duration: Long)
    fun trackApiCall(endpoint: String, duration: Long, success: Boolean)
}
```

### Accessibility

- [ ] **Content Descriptions**: All images have meaningful contentDescription
- [ ] **Touch Targets**: Minimum 48dp touch targets
- [ ] **Text Scaling**: Support for large text sizes
- [ ] **Color Contrast**: WCAG AA compliance
- [ ] **Screen Readers**: Test with TalkBack

### Localization

- [ ] **String Resources**: All strings externalized
- [ ] **Number Formatting**: Use Locale-aware formatting
- [ ] **Date/Time**: Use system locale for formatting
- [ ] **RTL Support**: Layout direction support for RTL languages

---

## Implementation Notes

### Dependency Injection (Hilt)

```kotlin
// Application class
@HiltAndroidApp
class ProductSearchApplication : Application()

// Module example
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(LoggingInterceptor())
            .addInterceptor(RetryInterceptor())
            .cache(Cache(cacheDir, 10 * 1024 * 1024))
            .build()
    }
    
    @Provides
    @Singleton
    fun provideProductApi(okHttpClient: OkHttpClient): ProductApi {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(ProductApi::class.java)
    }
}
```

### Navigation Setup

```kotlin
// MainActivity
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ProductSearchTheme {
                val navController = rememberNavController()
                NavHost(
                    navController = navController,
                    startDestination = "search"
                ) {
                    composable("search") {
                        SearchScreen(navController = navController)
                    }
                    composable("product_detail/{productId}") { backStackEntry ->
                        val productId = backStackEntry.arguments?.getString("productId") ?: return@composable
                        ProductDetailScreen(productId = productId)
                    }
                }
            }
        }
    }
}
```

