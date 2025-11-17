# Android Product Search App – Architecture

## 1. Overview

The application is a lightweight Android showcase built with Kotlin, Jetpack Compose, Paging 3, and a feature-based modular structure. It exposes two end-user capabilities:

- Searching the BestBuy catalog through a paged list.
- Inspecting a product’s detailed information.

Navigation is handled inside a single `MainActivity` by swapping the `SearchScreen` and `ProductDetailScreen` composables based on user selection. Business logic is organized with a Repository + Use Case pattern, while dependency management relies on Koin.

---

## 2. Module & Package Layout

```
app/
├── src/main/java/com/company/productsearch/ProductSearchApplication.kt
├── src/main/java/com/company/productsearch/di/AppModule.kt
└── src/main/java/com/example/technicalassesmentbyaman/MainActivity.kt

core/
├── common/
│   ├── config/AppConfig.kt
│   └── di/CommonModule.kt
├── data/
│   ├── di/DataModule.kt
│   ├── remote/ProductApi.kt + DTOs/Mappers
│   └── repository/ProductRepositoryImpl.kt
└── domain/
    ├── di/DomainModule.kt
    ├── model/Product.kt, ProductDetails.kt
    ├── repository/ProductRepository.kt
    └── usecase/SearchProductsUseCase.kt, GetProductDetailsUseCase.kt

feature/
├── search/
│   ├── di/SearchModule.kt
│   └── ui/SearchScreen.kt, SearchViewModel.kt, SearchPagingSource.kt, ProductItem.kt
└── productdetail/
    ├── di/ProductDetailModule.kt
    └── ui/ProductDetailScreen.kt, ProductDetailViewModel.kt
```

Each Android module owns its resources, Gradle configuration, and dependency graph bindings, keeping feature code isolated.

---

## 3. Module Responsibilities

- `app`: Initializes Koin (`ProductSearchApplication`), hosts the Compose hierarchy in `MainActivity`, and wires the two feature screens without a navigation component.
- `core:common`: Defines app-wide configuration via `AppConfig` and wires a Ktor `HttpClient` (OkHttp engine, ContentNegotiation, Logging). There are no Room/database or UI utilities at present.
- `core:data`: Implements the remote API contract (`ProductApiImpl`) and the repository layer (`ProductRepositoryImpl`). Mapping from DTOs to domain models lives alongside the remote definitions.
- `core:domain`: Holds pure Kotlin data models, the `ProductRepository` interface, and use cases that enforce lightweight input validation plus configuration defaults.
- `feature:search`: Presents the search UI and handles pagination. `SearchViewModel` manages query state, triggers searches, and exposes `Flow<PagingData<Product>>` backed by `SearchPagingSource`, which calls the `SearchProductsUseCase`.
- `feature:productdetail`: Displays a product’s expanded information. `ProductDetailViewModel` invokes `GetProductDetailsUseCase`, collects the result into `StateFlow`, and the Compose screen renders loading/error/success states.

---

## 4. Key Application Flows

### Search
```
SearchScreen (Compose) → SearchViewModel.performSearch()
    → MutableSharedFlow<String> search requests
    → SearchPagingSource(query, useCase, config)
    → SearchProductsUseCase(query, lang?, page?, pageSize?)
    → ProductRepository.searchProducts(...)
    → ProductApi.searchProducts(...)
```
`SearchPagingSource` builds paging metadata (next/prev page keys) based on API responses. The UI observes `LazyPagingItems`, reacts to loading states with progress indicators, and surfaces errors via snackbars.

### Product Detail
```
ProductDetailScreen → ProductDetailViewModel.loadProductDetails(productId)
    → GetProductDetailsUseCase(productId, lang)
    → ProductRepository.getProductDetails(...)
    → ProductApi.getProductDetails(...)
```
The ViewModel tracks a simple `ProductDetailUiState` (data/loading/error). The screen triggers detail loading through `LaunchedEffect` and shows snackbars for failures.

---

## 5. Dependency Injection

All dependencies are registered with Koin:

- `configModule`: `AppConfig` singleton (`AppConfigImpl`).
- `networkModule`: Configured Ktor `HttpClient`.
- `dataModule`: `ProductApi` + `ProductRepository` singletons.
- `domainModule`: Factories for `SearchProductsUseCase` and `GetProductDetailsUseCase`.
- `searchModule` / `productDetailModule`: Feature ViewModel bindings.

`ProductSearchApplication` starts Koin with the full list of modules. `MainActivity` and Compose screens resolve feature ViewModels through `koinViewModel()`.

---

## 6. Error Handling & UI State

- Network exceptions are captured in the repository and wrapped in `Result`.
- Use cases bubble the `Result` back to ViewModels.
- `SearchScreen` inspects `LoadState.Error` from `LazyPagingItems` and shows a snackbar with either the server-provided message or a localized fallback.
- `ProductDetailViewModel` stores the exception message inside `ProductDetailUiState` and the screen consumes/clears it to avoid duplicate snackbars.
- There is no bespoke retry helper yet; the Compose UI allows the user to trigger another search or reopen the detail screen.

---

## 7. Testing

Unit coverage currently focuses on the domain layer. Example: `GetProductDetailsUseCaseTest` verifies input validation and repository delegation with MockK. Additional tests (Paging source, repository integration, Compose UI) can be added incrementally; the architecture keeps pure Kotlin layers free of Android dependencies to ease testing.

---

## 8. Future Caching Strategy

Caching is not implemented today—the repository always hits the remote API. A pragmatic next iteration could follow these steps:

1. Introduce a Room database inside `core:data` with entities for summarized products and detailed entries.
2. Replace `SearchPagingSource` with a Pager backed by Room plus a `RemoteMediator` to synchronize pages while persisting scrollable results locally.
3. Enhance `ProductRepository` to read from Room first (both list and detail) and update entries after each successful network call.
4. Record retrieval timestamps per query/sku; if data is older than a configurable SLA, fetch in the background while still showing cached values (stale-while-revalidate).
5. Surface cache state to the UI (e.g., badge indicating offline data) and add a manual refresh option.

This approach would improve offline resilience, reduce API usage, and align nicely with the existing modular boundaries (Room stays in `core:data`, domain contracts remain unchanged).