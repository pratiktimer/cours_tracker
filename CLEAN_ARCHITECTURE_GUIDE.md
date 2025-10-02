# Complete Clean Architecture + TDD + MVVM Implementation Guide

## Overview
This guide provides a complete migration strategy for the Course Tracker app following Clean Architecture principles, MVVM pattern, and Test-Driven Development.

## Architecture Layers

### 1. Domain Layer (Business Logic - Pure Kotlin)
- **Models**: Business entities
- **Repositories**: Interfaces
- **Use Cases**: Business logic

### 2. Data Layer (Data Management)
- **Entities**: Room database entities
- **DAOs**: Database access
- **Repositories**: Implementation
- **Mappers**: Convert between layers

### 3. Presentation Layer (UI)
- **ViewModels**: UI logic
- **States**: UI states
- **Screens**: Composable UI

## Key Files to Create

### Domain Layer
1. `domain/model/Course.kt` - Business model
2. `domain/model/Video.kt` - Business model
3. `domain/repository/CourseRepository.kt` - Interface
4. `domain/usecase/GetCoursesUseCase.kt`
5. `domain/usecase/LoadCoursesFromFolderUseCase.kt`
6. `domain/usecase/UpdateVideoCompletionUseCase.kt`
7. `domain/usecase/GetVideosForCourseUseCase.kt`

### Data Layer
1. `data/local/entity/CourseEntity.kt`
2. `data/local/entity/VideoEntity.kt`
3. `data/local/entity/ParentFolderEntity.kt`
4. `data/local/dao/CourseDao.kt`
5. `data/local/dao/VideoDao.kt`
6. `data/local/dao/ParentFolderDao.kt`
7. `data/local/AppDatabase.kt`
8. `data/mapper/CourseMapper.kt`
9. `data/repository/CourseRepositoryImpl.kt`

### Presentation Layer
1. `presentation/courselist/CourseListViewModel.kt`
2. `presentation/courselist/CourseListState.kt`
3. `presentation/courselist/CourseListScreen.kt`
4. `presentation/videolist/VideoListViewModel.kt`
5. `presentation/videolist/VideoListState.kt`
6. `presentation/videolist/VideoListScreen.kt`

### DI Layer
1. `di/AppModule.kt`
2. `di/DatabaseModule.kt`
3. `di/RepositoryModule.kt`

### Test Files
1. `test/.../domain/usecase/GetCoursesUseCaseTest.kt`
2. `test/.../domain/usecase/UpdateVideoCompletionUseCaseTest.kt`
3. `test/.../data/repository/CourseRepositoryImplTest.kt`
4. `test/.../presentation/CourseListViewModelTest.kt`

## Dependencies to Add

```kotlin
// In gradle/libs.versions.toml
[versions]
hilt = "2.51"
mockk = "1.13.8"
coroutinesTest = "1.7.3"
turbine = "1.0.0"
truth = "1.1.5"

[libraries]
hilt-android = { group = "com.google.dagger", name = "hilt-android", version.ref = "hilt" }
hilt-compiler = { group = "com.google.dagger", name = "hilt-compiler", version.ref = "hilt" }
hilt-navigation-compose = { group = "androidx.hilt", name = "hilt-navigation-compose", version = "1.2.0" }
mockk = { group = "io.mockk", name = "mockk", version.ref = "mockk" }
kotlinx-coroutines-test = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-test", version.ref = "coroutinesTest" }
turbine = { group = "app.cash.turbine", name = "turbine", version.ref = "turbine" }
truth = { group = "com.google.truth", name = "truth", version.ref = "truth" }

[plugins]
hilt = { id = "com.google.dagger.hilt.android", version.ref = "hilt" }
```

```kotlin
// In app/build.gradle.kts - add these plugins
plugins {
    // ... existing plugins
    alias(libs.plugins.hilt)
}

// Add these dependencies
dependencies {
    // ... existing dependencies
    
    // Hilt
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)
    
    // ViewModel
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    
    // Testing
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    testImplementation(libs.truth)
}
```

## Implementation Strategy

### Step 1: Write Tests First (TDD)
Before implementing each component, write tests:
- Test use cases
- Test repository implementations
- Test ViewModels

### Step 2: Implement Domain Layer
Pure Kotlin, no Android dependencies

### Step 3: Implement Data Layer
Room database, repositories

### Step 4: Implement Presentation Layer
ViewModels, UI States, Compose screens

### Step 5: Setup DI with Hilt
Wire everything together

### Step 6: Update MainActivity
Remove business logic, keep only UI navigation

## Benefits

1. **Testability**: Each layer can be tested independently
2. **Maintainability**: Clear separation of concerns
3. **Scalability**: Easy to add new features
4. **Reusability**: Business logic independent of Android
5. **TDD**: Tests drive the implementation

## Next Steps

1. Review this guide
2. Update dependencies
3. Create domain layer files (I will provide complete code)
4. Create tests for domain layer
5. Implement remaining layers
6. Migrate existing code gradually

