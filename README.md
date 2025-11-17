# Android Product Search App

Minimal product search experience built with Kotlin, Jetpack Compose, Paging 3, and Koin. The app queries the public BestBuy catalog, lets users browse paged results, and opens a dedicated detail screen for any product.

## Features
- Search products with client-side paging and inline loading/error states.
- Persist scroll position when returning from the detail screen.
- Detailed view with media, pricing, availability, and specs rendered via Compose.
- Modularized Gradle setup (`app`, `core/*`, `feature/*`) for clear ownership of UI, domain, and data layers.

## Architecture Snapshot
- **Presentation**: Compose screens + ViewModels per feature (`SearchViewModel`, `ProductDetailViewModel`).
- **Domain**: Use cases (`SearchProductsUseCase`, `GetProductDetailsUseCase`) and models (`Product`, `ProductDetails`).
- **Data**: Ktor-powered `ProductApi` and `ProductRepositoryImpl` that maps DTOs into domain objects.
- **DI**: Koin modules per layer; bootstrapped from `ProductSearchApplication`.
- **Navigation**: Single `MainActivity` toggles between Search and Detail screens without a nav graph.

See `ARCHITECTURE.md` for the full breakdown and future caching plan.

## Tech Stack
- Kotlin, Coroutines, Flow
- Jetpack Compose + Material 3
- Paging 3 (manual `PagingSource`)
- Ktor (OkHttp engine) with Kotlinx Serialization
- Koin for dependency injection
- Coil for image loading
- Unit tests: JUnit 4, MockK, kotlinx-coroutines-test

## Project Structure
```
app/                   // Application entry + MainActivity
core/common/           // AppConfig + Ktor HttpClient setup
core/data/             // ProductApi, DTOs, repository implementation
core/domain/           // Models, repository interface, use cases
feature/search/        // Search UI, PagingSource, ViewModel
feature/productdetail/ // Detail UI and ViewModel
```

## Getting Started
1. **Prerequisites**  
   - Android Studio: This project is built on Android Studio Narwhal 4 Feature Drop | 2025.1.4
   - Android SDK 34
2. **Clone & Sync**
   ```bash
   git clone /Users/amanshaikh/Essentials/Development/Technical-Assesment-By-Aman
   cd Technical-Assesment-By-Aman
   ./gradlew clean
   ```
3. **Run the app** via Android Studio (“Run > Run ‘app’”) or:
   ```bash
   ./gradlew :app:assembleDebug
   ```
4. **Install** the generated APK from `app/build/outputs/apk/debug/`.

## Tests
- Use case tests live under `core/domain/src/test`.
- ViewModel tests live under `feature/search/src/test` and `feature/productdetail/src/test`.
- To run them in Android Studio, open the desired test class and click the green Run/Debug arrow in the gutter or use the “Run Tests” (play button) toolbar action for the module/package.

## Future Enhancements
- Add Room-backed caching plus a Paging 3 `RemoteMediator`.
- Introduce Navigation Compose for clearer back stack handling.
- Expand test coverage to Paging sources, repositories, and Compose UI.
- Add offline indicators and retry affordances around network calls.

