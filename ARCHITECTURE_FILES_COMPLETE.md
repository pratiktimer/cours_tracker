# Complete Clean Architecture + TDD + MVVM Implementation

## Table of Contents
1. [Dependencies Setup](#dependencies-setup)
2. [Domain Layer](#domain-layer)
3. [Data Layer](#data-layer)
4. [Presentation Layer](#presentation-layer)
5. [DI Layer](#di-layer)
6. [Tests](#tests)
7. [Migration Steps](#migration-steps)

---

## 1. Dependencies Setup

### gradle/libs.versions.toml

``toml
[versions]
agp = "8.13.0"
kotlin = "2.0.21"
coreKtx = "1.10.1"
junit = "4.13.2"
junitVersion = "1.1.5"
espressoCore = "3.5.1"
lifecycleRuntimeKtx = "2.6.1"
activityCompose = "1.8.0"
composeBom = "2024.09.00"
roomKtx = "2.8.1"
documentfile = "1.1.0"
foundationLayout = "1.9.2"
roomRuntime = "2.8.1"
navigationCompose = "2.9.5"
hilt = "2.51"
mockk = "1.13.8"
coroutinesTest = "1.7.3"
turbine = "1.0.0"
truth = "1.1.5"
lifecycleViewModel = "2.6.1"
coil = "2.7.0"

[libraries]
androidx-core-ktx = { group = "androidx.core", name = "core-ktx", version.ref = "coreKtx" }
androidx-room-runtime = { module = "androidx.room:room-runtime", version.ref = "roomRuntime" }
androidx-room-compiler = { module = "androidx.room:room-compiler", version.ref = "roomRuntime" }
androidx-room-ktx = { group = "androidx.room", name = "room-ktx", version.ref = "roomKtx" }
junit = { group = "junit", name = "junit", version.ref = "junit" }
androidx-junit = { group = "androidx.test.ext", name = "junit", version.ref = "junitVersion" }
androidx-espresso-core = { group = "androidx.test.espresso", name = "espresso-core", version.ref = "espressoCore" }
androidx-lifecycle-runtime-ktx = { group = "androidx.lifecycle", name = "lifecycle-runtime-ktx", version.ref = "lifecycleRuntimeKtx" }
androidx-lifecycle-viewmodel-ktx = { group = "androidx.lifecycle", name = "lifecycle-viewmodel-ktx", version.ref = "lifecycleViewModel" }
androidx-lifecycle-viewmodel-compose = { group = "androidx.lifecycle", name = "lifecycle-viewmodel-compose", version.ref = "lifecycleViewModel" }
androidx-activity-compose = { group = "androidx.activity", name = "activity-compose", version.ref = "activityCompose" }
androidx-compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "composeBom" }
androidx-compose-ui = { group = "androidx.compose.ui", name = "ui" }
androidx-compose-ui-graphics = { group = "androidx.compose.ui", name = "ui-graphics" }
androidx-compose-ui-tooling = { group = "androidx.compose.ui", name = "ui-tooling" }
androidx-compose-ui-tooling-preview = { group = "androidx.compose.ui", name = "ui-tooling-preview" }
androidx-compose-ui-test-manifest = { group = "androidx.compose.ui", name = "ui-test-manifest" }
androidx-compose-ui-test-junit4 = { group = "androidx.compose.ui", name = "ui-test-junit4" }
androidx-compose-material3 = { group = "androidx.compose.material3", name = "material3" }
androidx-compose-material = { group = "androidx.compose.material", name = "material", version = "1.5.0" }
androidx-documentfile = { group = "androidx.documentfile", name = "documentfile", version.ref = "documentfile" }
androidx-compose-foundation-layout = { group = "androidx.compose.foundation", name = "foundation-layout", version.ref = "foundationLayout" }
androidx-navigation-compose = { group = "androidx.navigation", name = "navigation-compose", version.ref = "navigationCompose" }
hilt-android = { group = "com.google.dagger", name = "hilt-android", version.ref = "hilt" }
hilt-compiler = { group = "com.google.dagger", name = "hilt-compiler", version.ref = "hilt" }
hilt-navigation-compose = { group = "androidx.hilt", name = "hilt-navigation-compose", version = "1.2.0" }
mockk = { group = "io.mockk", name = "mockk", version.ref = "mockk" }
kotlinx-coroutines-test = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-test", version.ref = "coroutinesTest" }
turbine = { group = "app.cash.turbine", name = "turbine", version.ref = "turbine" }
truth = { group = "com.google.truth", name = "truth", version.ref = "truth" }
coil-compose = { group = "io.coil-kt", name = "coil-compose", version.ref = "coil" }

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
kotlin-compose = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
kotlin-kapt = { id = "kotlin-kapt", version.ref = "kotlin" }
hilt = { id = "com.google.dagger.hilt.android", version.ref = "hilt" }
``

### app/build.gradle.kts

``kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt) // Add Hilt
    id("kotlin-kapt")
    id("kotlin-parcelize")
}

android {
    namespace = "com.prateektimer.course_tracker"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.prateektimer.course_tracker"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
        freeCompilerArgs += "-Xparcelize"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    // Core Android
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    
    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material)
    implementation(libs.androidx.compose.foundation.layout)
    implementation(libs.androidx.navigation.compose)
    
    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    kapt(libs.androidx.room.compiler)
    
    // Hilt
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)
    
    // ViewModel
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    
    // Document File & Coil
    implementation(libs.androidx.documentfile)
    implementation(libs.coil.compose)
    
    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    testImplementation(libs.truth)
    
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
``

---

## 2. Domain Layer

### domain/model/Video.kt

``kotlin
package com.prateektimer.course_tracker.domain.model

import android.net.Uri

data class Video(
    val id: String,
    val uri: Uri,
    val isComplete: Boolean = false
)
``

### domain/model/Course.kt

``kotlin
package com.prateektimer.course_tracker.domain.model

data class Course(
    val id: String,
    val name: String,
    val thumbnailPath: String? = null,
    val videos: List<Video> = emptyList()
) {
    val completionPercentage: Float
        get() = if (videos.isEmpty()) 0f 
                else videos.count { it.isComplete }.toFloat() / videos.size
    
    val completedCount: Int
        get() = videos.count { it.isComplete }
}
``

### domain/repository/CourseRepository.kt

``kotlin
package com.prateektimer.course_tracker.domain.repository

import android.app.Activity
import android.net.Uri
import com.prateektimer.course_tracker.domain.model.Course
import com.prateektimer.course_tracker.domain.model.Video
import kotlinx.coroutines.flow.Flow

interface CourseRepository {
    fun getAllCourses(): Flow<List<Course>>
    suspend fun getCourseById(courseId: String): Course?
    suspend fun loadCoursesFromFolder(activity: Activity, folderUri: Uri)
    suspend fun updateVideoCompletion(courseId: String, videoId: String, isComplete: Boolean)
    suspend fun saveCourses(courses: List<Course>)
    suspend fun getVideosForCourse(courseId: String): List<Video>
}
``

### domain/usecase/GetCoursesUseCase.kt

``kotlin
package com.prateektimer.course_tracker.domain.usecase

import com.prateektimer.course_tracker.domain.model.Course
import com.prateektimer.course_tracker.domain.repository.CourseRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCoursesUseCase @Inject constructor(
    private val repository: CourseRepository
) {
    operator fun invoke(): Flow<List<Course>> {
        return repository.getAllCourses()
    }
}
``

### domain/usecase/GetCourseByIdUseCase.kt

``kotlin
package com.prateektimer.course_tracker.domain.usecase

import com.prateektimer.course_tracker.domain.model.Course
import com.prateektimer.course_tracker.domain.repository.CourseRepository
import javax.inject.Inject

class GetCourseByIdUseCase @Inject constructor(
    private val repository: CourseRepository
) {
    suspend operator fun invoke(courseId: String): Course? {
        return repository.getCourseById(courseId)
    }
}
``

### domain/usecase/LoadCoursesFromFolderUseCase.kt

``kotlin
package com.prateektimer.course_tracker.domain.usecase

import android.app.Activity
import android.net.Uri
import com.prateektimer.course_tracker.domain.repository.CourseRepository
import javax.inject.Inject

class LoadCoursesFromFolderUseCase @Inject constructor(
    private val repository: CourseRepository
) {
    suspend operator fun invoke(activity: Activity, folderUri: Uri) {
        repository.loadCoursesFromFolder(activity, folderUri)
    }
}
``

### domain/usecase/UpdateVideoCompletionUseCase.kt

``kotlin
package com.prateektimer.course_tracker.domain.usecase

import com.prateektimer.course_tracker.domain.repository.CourseRepository
import javax.inject.Inject

class UpdateVideoCompletionUseCase @Inject constructor(
    private val repository: CourseRepository
) {
    suspend operator fun invoke(courseId: String, videoId: String, isComplete: Boolean) {
        repository.updateVideoCompletion(courseId, videoId, isComplete)
    }
}
``

---

## 3. Data Layer

### data/local/entity/CourseEntity.kt

``kotlin
package com.prateektimer.course_tracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "courses")
data class CourseEntity(
    @PrimaryKey val id: String,
    val name: String,
    val thumbnailUri: String?
)
``

### data/local/entity/VideoEntity.kt

``kotlin
package com.prateektimer.course_tracker.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "videos",
    foreignKeys = [ForeignKey(
        entity = CourseEntity::class,
        parentColumns = ["id"],
        childColumns = ["courseId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class VideoEntity(
    @PrimaryKey val id: String,
    val courseId: String,
    val uri: String,
    val isComplete: Boolean = false
)
``

### data/local/entity/ParentFolderEntity.kt

``kotlin
package com.prateektimer.course_tracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "parent_folder")
data class ParentFolderEntity(
    @PrimaryKey val id: Int = 0,
    val uri: String
)
``

### data/local/dao/CourseDao.kt

``kotlin
package com.prateektimer.course_tracker.data.local.dao

import androidx.room.*
import com.prateektimer.course_tracker.data.local.entity.CourseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CourseDao {
    @Query("SELECT * FROM courses")
    fun getAllCourses(): Flow<List<CourseEntity>>
    
    @Query("SELECT * FROM courses WHERE id = :courseId")
    suspend fun getCourseById(courseId: String): CourseEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourses(courses: List<CourseEntity>)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourse(course: CourseEntity)
    
    @Query("DELETE FROM courses")
    suspend fun deleteAll()
}
``

### data/local/dao/VideoDao.kt

``kotlin
package com.prateektimer.course_tracker.data.local.dao

import androidx.room.*
import com.prateektimer.course_tracker.data.local.entity.VideoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VideoDao {
    @Query("SELECT * FROM videos WHERE courseId = :courseId")
    suspend fun getVideosForCourse(courseId: String): List<VideoEntity>
    
    @Query("SELECT * FROM videos WHERE courseId = :courseId")
    fun getVideosForCourseFlow(courseId: String): Flow<List<VideoEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideos(videos: List<VideoEntity>)
    
    @Update
    suspend fun updateVideo(video: VideoEntity)
    
    @Query("UPDATE videos SET isComplete = :isComplete WHERE id = :videoId")
    suspend fun updateVideoCompletion(videoId: String, isComplete: Boolean)
}
``

### data/local/dao/ParentFolderDao.kt

``kotlin
package com.prateektimer.course_tracker.data.local.dao

import androidx.room.*
import com.prateektimer.course_tracker.data.local.entity.ParentFolderEntity

@Dao
interface ParentFolderDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(folder: ParentFolderEntity)
    
    @Query("SELECT * FROM parent_folder LIMIT 1")
    suspend fun getFolder(): ParentFolderEntity?
}
``

### data/local/AppDatabase.kt

``kotlin
package com.prateektimer.course_tracker.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.prateektimer.course_tracker.data.local.dao.CourseDao
import com.prateektimer.course_tracker.data.local.dao.ParentFolderDao
import com.prateektimer.course_tracker.data.local.dao.VideoDao
import com.prateektimer.course_tracker.data.local.entity.CourseEntity
import com.prateektimer.course_tracker.data.local.entity.ParentFolderEntity
import com.prateektimer.course_tracker.data.local.entity.VideoEntity

@Database(
    entities = [CourseEntity::class, VideoEntity::class, ParentFolderEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun courseDao(): CourseDao
    abstract fun videoDao(): VideoDao
    abstract fun parentFolderDao(): ParentFolderDao
}
``

### data/mapper/CourseMapper.kt

``kotlin
package com.prateektimer.course_tracker.data.mapper

import android.net.Uri
import com.prateektimer.course_tracker.data.local.entity.CourseEntity
import com.prateektimer.course_tracker.data.local.entity.VideoEntity
import com.prateektimer.course_tracker.domain.model.Course
import com.prateektimer.course_tracker.domain.model.Video

fun CourseEntity.toDomain(videos: List<Video>): Course {
    return Course(
        id = id,
        name = name,
        thumbnailPath = thumbnailUri,
        videos = videos
    )
}

fun Course.toEntity(): CourseEntity {
    return CourseEntity(
        id = id,
        name = name,
        thumbnailUri = thumbnailPath
    )
}

fun VideoEntity.toDomain(): Video {
    return Video(
        id = id,
        uri = Uri.parse(uri),
        isComplete = isComplete
    )
}

fun Video.toEntity(courseId: String): VideoEntity {
    return VideoEntity(
        id = id,
        courseId = courseId,
        uri = uri.toString(),
        isComplete = isComplete
    )
}
``

### data/repository/CourseRepositoryImpl.kt

``kotlin
package com.prateektimer.course_tracker.data.repository

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.prateektimer.course_tracker.data.local.dao.CourseDao
import com.prateektimer.course_tracker.data.local.dao.VideoDao
import com.prateektimer.course_tracker.data.mapper.toDomain
import com.prateektimer.course_tracker.data.mapper.toEntity
import com.prateektimer.course_tracker.domain.model.Course
import com.prateektimer.course_tracker.domain.model.Video
import com.prateektimer.course_tracker.domain.repository.CourseRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import javax.inject.Inject

class CourseRepositoryImpl @Inject constructor(
    private val courseDao: CourseDao,
    private val videoDao: VideoDao,
    @ApplicationContext private val context: Context
) : CourseRepository {

    override fun getAllCourses(): Flow<List<Course>> {
        return courseDao.getAllCourses().map { courseEntities ->
            courseEntities.map { courseEntity ->
                val videos = videoDao.getVideosForCourse(courseEntity.id).map { it.toDomain() }
                courseEntity.toDomain(videos)
            }
        }
    }

    override suspend fun getCourseById(courseId: String): Course? {
        return withContext(Dispatchers.IO) {
            val courseEntity = courseDao.getCourseById(courseId) ?: return@withContext null
            val videos = videoDao.getVideosForCourse(courseId).map { it.toDomain() }
            courseEntity.toDomain(videos)
        }
    }

    override suspend fun loadCoursesFromFolder(activity: Activity, folderUri: Uri) {
        withContext(Dispatchers.IO) {
            val parent = DocumentFile.fromTreeUri(activity, folderUri) ?: return@withContext
            val loadedCourses = mutableListOf<Course>()

            parent.listFiles()
                .filter { it.isDirectory }
                .sortedWith(compareBy(naturalSortComparator()) { it.name ?: "" })
                .forEach { subFolder ->
                    val videoFiles = subFolder.listFiles()
                        .filter { it.isFile && it.name?.endsWith(".mp4", true) == true }
                        .sortedWith(compareBy(naturalSortComparator()) { it.name ?: "" })
                        .map { Video(id = UUID.randomUUID().toString(), uri = it.uri) }

                    if (videoFiles.isNotEmpty()) {
                        val course = Course(
                            id = UUID.randomUUID().toString(),
                            name = subFolder.name ?: "Untitled",
                            videos = videoFiles
                        )
                        loadedCourses.add(course)
                    }
                }

            saveCourses(loadedCourses)
        }
    }

    override suspend fun updateVideoCompletion(courseId: String, videoId: String, isComplete: Boolean) {
        withContext(Dispatchers.IO) {
            videoDao.updateVideoCompletion(videoId, isComplete)
        }
    }

    override suspend fun saveCourses(courses: List<Course>) {
        withContext(Dispatchers.IO) {
            val courseEntities = courses.map { it.toEntity() }
            val videoEntities = courses.flatMap { course ->
                course.videos.map { video -> video.toEntity(course.id) }
            }
            courseDao.insertCourses(courseEntities)
            videoDao.insertVideos(videoEntities)
        }
    }

    override suspend fun getVideosForCourse(courseId: String): List<Video> {
        return withContext(Dispatchers.IO) {
            videoDao.getVideosForCourse(courseId).map { it.toDomain() }
        }
    }

    private fun naturalSortComparator(): Comparator<String> {
        val regex = "\\d+".toRegex()
        return Comparator { a, b ->
            if (a == null && b == null) return@Comparator 0
            if (a == null) return@Comparator -1
            if (b == null) return@Comparator 1

            val aParts = regex.findAll(a).map { it.value }.toList()
            val bParts = regex.findAll(b).map { it.value }.toList()

            val aNums = aParts.mapNotNull { it.toIntOrNull() }
            val bNums = bParts.mapNotNull { it.toIntOrNull() }

            if (aNums.isNotEmpty() && bNums.isNotEmpty()) {
                val cmp = aNums[0].compareTo(bNums[0])
                if (cmp != 0) return@Comparator cmp
            }

            return@Comparator a.compareTo(b, ignoreCase = true)
        }
    }
}
``

---

## 4. Presentation Layer

### presentation/courselist/CourseListState.kt

``kotlin
package com.prateektimer.course_tracker.presentation.courselist

import com.prateektimer.course_tracker.domain.model.Course

data class CourseListState(
    val courses: List<Course> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
``

### presentation/courselist/CourseListViewModel.kt

``kotlin
package com.prateektimer.course_tracker.presentation.courselist

import android.app.Activity
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prateektimer.course_tracker.domain.usecase.GetCoursesUseCase
import com.prateektimer.course_tracker.domain.usecase.LoadCoursesFromFolderUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CourseListViewModel @Inject constructor(
    private val getCoursesUseCase: GetCoursesUseCase,
    private val loadCoursesFromFolderUseCase: LoadCoursesFromFolderUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(CourseListState())
    val state: StateFlow<CourseListState> = _state.asStateFlow()

    init {
        loadCourses()
    }

    private fun loadCourses() {
        viewModelScope.launch {
            getCoursesUseCase()
                .catch { e ->
                    _state.update { it.copy(error = e.message, isLoading = false) }
                }
                .collect { courses ->
                    _state.update { it.copy(courses = courses, isLoading = false) }
                }
        }
    }

    fun loadFromFolder(activity: Activity, folderUri: Uri) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                loadCoursesFromFolderUseCase(activity, folderUri)
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }
}
``

### presentation/videolist/VideoListState.kt

``kotlin
package com.prateektimer.course_tracker.presentation.videolist

import com.prateektimer.course_tracker.domain.model.Course

data class VideoListState(
    val course: Course? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)
``

### presentation/videolist/VideoListViewModel.kt

``kotlin
package com.prateektimer.course_tracker.presentation.videolist

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prateektimer.course_tracker.domain.usecase.GetCourseByIdUseCase
import com.prateektimer.course_tracker.domain.usecase.UpdateVideoCompletionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VideoListViewModel @Inject constructor(
    private val getCourseByIdUseCase: GetCourseByIdUseCase,
    private val updateVideoCompletionUseCase: UpdateVideoCompletionUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val courseId: String = savedStateHandle.get<String>("courseId") ?: ""

    private val _state = MutableStateFlow(VideoListState())
    val state: StateFlow<VideoListState> = _state.asStateFlow()

    init {
        loadCourse()
    }

    private fun loadCourse() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val course = getCourseByIdUseCase(courseId)
                _state.update { it.copy(course = course, isLoading = false) }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    fun toggleVideoCompletion(videoId: String, currentStatus: Boolean) {
        viewModelScope.launch {
            try {
                updateVideoCompletionUseCase(courseId, videoId, !currentStatus)
                loadCourse() // Reload to get updated data
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message) }
            }
        }
    }
}
``

---

## 5. DI Layer

### di/DatabaseModule.kt

``kotlin
package com.prateektimer.course_tracker.di

import android.content.Context
import androidx.room.Room
import com.prateektimer.course_tracker.data.local.AppDatabase
import com.prateektimer.course_tracker.data.local.dao.CourseDao
import com.prateektimer.course_tracker.data.local.dao.ParentFolderDao
import com.prateektimer.course_tracker.data.local.dao.VideoDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "course_db"
        ).build()
    }

    @Provides
    fun provideCourseDao(database: AppDatabase): CourseDao {
        return database.courseDao()
    }

    @Provides
    fun provideVideoDao(database: AppDatabase): VideoDao {
        return database.videoDao()
    }

    @Provides
    fun provideParentFolderDao(database: AppDatabase): ParentFolderDao {
        return database.parentFolderDao()
    }
}
``

### di/RepositoryModule.kt

``kotlin
package com.prateektimer.course_tracker.di

import com.prateektimer.course_tracker.data.repository.CourseRepositoryImpl
import com.prateektimer.course_tracker.domain.repository.CourseRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindCourseRepository(
        courseRepositoryImpl: CourseRepositoryImpl
    ): CourseRepository
}
``

### Application Class

Create CourseTrackerApplication.kt in the root package:

``kotlin
package com.prateektimer.course_tracker

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class CourseTrackerApplication : Application()
``

Update AndroidManifest.xml:

``xml
<application
    android:name=".CourseTrackerApplication"
    ...>
``

---

## 6. Tests

### test/.../domain/usecase/GetCoursesUseCaseTest.kt

``kotlin
package com.prateektimer.course_tracker.domain.usecase

import android.net.Uri
import com.prateektimer.course_tracker.domain.model.Course
import com.prateektimer.course_tracker.domain.model.Video
import com.prateektimer.course_tracker.domain.repository.CourseRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class GetCoursesUseCaseTest {

    private lateinit var repository: CourseRepository
    private lateinit var useCase: GetCoursesUseCase

    @Before
    fun setup() {
        repository = mockk()
        useCase = GetCoursesUseCase(repository)
    }

    @Test
    fun invoke returns courses from repository() = runTest {
        // Given
        val mockUri = mockk<Uri>()
        val expectedCourses = listOf(
            Course(
                id = "1",
                name = "Course 1",
                videos = listOf(
                    Video("v1", mockUri, false)
                )
            )
        )
        every { repository.getAllCourses() } returns flowOf(expectedCourses)

        // When
        val result = useCase().toList()

        // Then
        assertEquals(1, result.size)
        assertEquals(expectedCourses, result[0])
    }

    @Test
    fun invoke returns empty list when no courses() = runTest {
        // Given
        every { repository.getAllCourses() } returns flowOf(emptyList())

        // When
        val result = useCase().toList()

        // Then
        assertEquals(1, result.size)
        assertEquals(emptyList(), result[0])
    }
}
``

### test/.../domain/usecase/UpdateVideoCompletionUseCaseTest.kt

``kotlin
package com.prateektimer.course_tracker.domain.usecase

import com.prateektimer.course_tracker.domain.repository.CourseRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class UpdateVideoCompletionUseCaseTest {

    private lateinit var repository: CourseRepository
    private lateinit var useCase: UpdateVideoCompletionUseCase

    @Before
    fun setup() {
        repository = mockk()
        useCase = UpdateVideoCompletionUseCase(repository)
    }

    @Test
    fun invoke calls repository updateVideoCompletion() = runTest {
        // Given
        val courseId = "course1"
        val videoId = "video1"
        val isComplete = true
        coEvery { repository.updateVideoCompletion(any(), any(), any()) } returns Unit

        // When
        useCase(courseId, videoId, isComplete)

        // Then
        coVerify(exactly = 1) { 
            repository.updateVideoCompletion(courseId, videoId, isComplete) 
        }
    }
}
``

### test/.../presentation/CourseListViewModelTest.kt

``kotlin
package com.prateektimer.course_tracker.presentation

import android.net.Uri
import app.cash.turbine.test
import com.prateektimer.course_tracker.domain.model.Course
import com.prateektimer.course_tracker.domain.model.Video
import com.prateektimer.course_tracker.domain.usecase.GetCoursesUseCase
import com.prateektimer.course_tracker.domain.usecase.LoadCoursesFromFolderUseCase
import com.prateektimer.course_tracker.presentation.courselist.CourseListViewModel
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

@OptIn(ExperimentalCoroutinesApi::class)
class CourseListViewModelTest {

    private lateinit var getCoursesUseCase: GetCoursesUseCase
    private lateinit var loadCoursesFromFolderUseCase: LoadCoursesFromFolderUseCase
    private lateinit var viewModel: CourseListViewModel

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        getCoursesUseCase = mockk()
        loadCoursesFromFolderUseCase = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initial state is empty() = runTest {
        // Given
        every { getCoursesUseCase() } returns flowOf(emptyList())

        // When
        viewModel = CourseListViewModel(getCoursesUseCase, loadCoursesFromFolderUseCase)

        // Then
        viewModel.state.test {
            val emission = awaitItem()
            assertEquals(emptyList(), emission.courses)
            assertFalse(emission.isLoading)
        }
    }

    @Test
    fun courses are loaded from use case() = runTest {
        // Given
        val mockUri = mockk<Uri>()
        val courses = listOf(
            Course("1", "Course 1", null, listOf(Video("v1", mockUri, false)))
        )
        every { getCoursesUseCase() } returns flowOf(courses)

        // When
        viewModel = CourseListViewModel(getCoursesUseCase, loadCoursesFromFolderUseCase)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        viewModel.state.test {
            val emission = awaitItem()
            assertEquals(courses, emission.courses)
        }
    }
}
``

---

## 7. Migration Steps

### Step 1: Backup Current Code
1. Commit all current changes to git
2. Create a new branch: git checkout -b feature/clean-architecture

### Step 2: Update Dependencies
1. Replace gradle/libs.versions.toml with the new version
2. Replace pp/build.gradle.kts with the updated version
3. Sync project

### Step 3: Create Directory Structure
All directories are already created. Now create files.

### Step 4: Create Domain Layer Files
Copy all domain layer code from above into respective files.

### Step 5: Create Data Layer Files
Copy all data layer code into respective files.

### Step 6: Create Presentation Layer Files
Copy presentation layer code.

### Step 7: Create DI Layer
Copy DI module code.

### Step 8: Create Application Class
Create CourseTrackerApplication.kt and update manifest.

### Step 9: Write and Run Tests
1. Create test files
2. Run tests: ./gradlew test
3. Fix any failing tests

### Step 10: Update UI Screens
1. Create new Compose screens using ViewModels
2. Update MainActivity to use Hilt and navigation

### Step 11: Remove Old Code
1. Once new architecture is working, remove old monolithic code
2. Clean up unused imports

### Step 12: Test Thoroughly
1. Run all tests
2. Manual testing on device/emulator
3. Test edge cases

---

## Summary

You now have a complete Clean Architecture implementation with:

- **Domain Layer**: Pure business logic
- **Data Layer**: Database and repository implementations
- **Presentation Layer**: MVVM with ViewModels and StateFlows
- **DI**: Hilt for dependency injection
- **Tests**: Unit tests for use cases, repository, and ViewModels

### Key Benefits:
1. **Testability**: Every component can be tested in isolation
2. **Maintainability**: Clear separation of concerns
3. **Scalability**: Easy to add new features
4. **Type Safety**: Proper models for each layer
5. **TDD Ready**: Tests drive the implementation

### Next Actions:
1. Copy all code files into your project
2. Create UI screens (CourseListScreen.kt, VideoListScreen.kt)
3. Update MainActivity to use Hilt and new ViewModels
4. Run tests and verify everything works
5. Gradually migrate remaining UI code
