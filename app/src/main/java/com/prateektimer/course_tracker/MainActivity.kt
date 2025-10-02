package com.prateektimer.course_tracker

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Checkbox
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.room.*
import coil.compose.AsyncImage
import com.prateektimer.course_tracker.ui.theme.Course_trackerTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.*

/** --------------------- ROOM ENTITIES --------------------- */
@Entity
data class ParentFolderEntity(
    @PrimaryKey val id: Int = 0,
    val uri: String
)

@Entity
data class CourseEntity(
    @PrimaryKey val id: String,
    val name: String,
    val thumbnailUri: String?
)

@Entity(
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

/** --------------------- ROOM DAOs --------------------- */
@Dao
interface ParentFolderDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(folder: ParentFolderEntity)

    @Query("SELECT * FROM ParentFolderEntity LIMIT 1")
    suspend fun getFolder(): ParentFolderEntity?
}

@Dao
interface CourseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourses(courses: List<CourseEntity>)

    @Query("SELECT * FROM CourseEntity")
    suspend fun getAllCourses(): List<CourseEntity>
}

@Dao
interface VideoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideos(videos: List<VideoEntity>)

    @Query("SELECT * FROM VideoEntity WHERE courseId = :courseId")
    suspend fun getVideosForCourse(courseId: String): List<VideoEntity>

    @Update
    suspend fun updateVideo(video: VideoEntity)
}

/** --------------------- DATABASE --------------------- */
@Database(entities = [ParentFolderEntity::class, CourseEntity::class, VideoEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun parentFolderDao(): ParentFolderDao
    abstract fun courseDao(): CourseDao
    abstract fun videoDao(): VideoDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "course_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

/** --------------------- MODELS --------------------- */
data class Video(
    val id: String = UUID.randomUUID().toString(),
    val uri: Uri,
    var isComplete: Boolean = false
)

data class Course(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    var thumbnailPath: String? = null,
    val videos: List<Video>,
    val thumbnailState: MutableState<String?> = mutableStateOf(null)
)

/** --------------------- VIEWMODEL --------------------- */
class CourseViewModel : ViewModel() {

    var courses = mutableStateListOf<Course>()
        private set

    fun setCourses(newCourses: List<Course>) {
        courses.clear()
        courses.addAll(newCourses)
    }

    fun updateVideo(courseId: String, videoId: String, isComplete: Boolean) {
        val courseIndex = courses.indexOfFirst { it.id == courseId }
        if (courseIndex != -1) {
            val course = courses[courseIndex]
            val updatedVideos = course.videos.map {
                if (it.id == videoId) it.copy(isComplete = isComplete) else it
            }
            courses[courseIndex] = course.copy(videos = updatedVideos)
        }
    }

    fun getCourse(id: String) = courses.find { it.id == id }

    fun loadCourseThumbnail(course: Course, context: Context) {
        if (course.thumbnailState.value != null) return
        val firstVideo = course.videos[1] ?: return

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val retriever = MediaMetadataRetriever()
                retriever.setDataSource(context, firstVideo.uri)
                val bitmap = retriever.frameAtTime
                retriever.release()

                bitmap?.let {
                    val file = File(context.cacheDir, "${course.id}_thumb.png")
                    FileOutputStream(file).use { out -> it.compress(Bitmap.CompressFormat.PNG, 100, out) }
                    val path = file.absolutePath

                    withContext(Dispatchers.Main) {
                        course.thumbnailPath = path
                        course.thumbnailState.value = path
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun loadCoursesFromParentFolder(activity: Activity, parentUri: Uri, db: AppDatabase) {
        viewModelScope.launch(Dispatchers.IO) {
            val parent = DocumentFile.fromTreeUri(activity, parentUri) ?: return@launch
            val loadedCourses = mutableListOf<Course>()

            parent.listFiles()
                .filter { it.isDirectory }
                .sortedWith(compareBy(naturalSortComparator()) { it.name ?: "" }) // 👈 natural sort
                .forEach { subFolder ->

                    val videoFiles = subFolder.listFiles()
                        .filter { it.isFile && it.name?.endsWith(".mp4", true) == true }
                        .sortedWith(compareBy(naturalSortComparator()) { it.name ?: "" }) // 👈 natural sort
                        .map { Video(uri = it.uri) }

                    if (videoFiles.isNotEmpty()) {
                        val course = Course(
                            id = UUID.randomUUID().toString(),
                            name = subFolder.name ?: "Untitled",
                            videos = videoFiles
                        )
                        loadedCourses.add(course)
                    }
                }

            // Save to Room
            val courseEntities = loadedCourses.map { c -> CourseEntity(c.id, c.name, thumbnailUri = c.thumbnailPath) }
            val videoEntities = loadedCourses.flatMap { c ->
                c.videos.map { v -> VideoEntity(v.id, c.id, v.uri.toString(), v.isComplete) }
            }
            db.courseDao().insertCourses(courseEntities)
            db.videoDao().insertVideos(videoEntities)

            withContext(Dispatchers.Main) {
                setCourses(loadedCourses)
            }
        }
    }
    fun naturalSortComparator(): Comparator<String> {
        val regex = "\\d+".toRegex()
        return Comparator { a, b ->
            if (a == null && b == null) return@Comparator 0
            if (a == null) return@Comparator -1
            if (b == null) return@Comparator 1

            val aParts = regex.findAll(a).map { it.value }.toList()
            val bParts = regex.findAll(b).map { it.value }.toList()

            val aNums = aParts.mapNotNull { it.toIntOrNull() }
            val bNums = bParts.mapNotNull { it.toIntOrNull() }

            // Compare numbers if both have them
            if (aNums.isNotEmpty() && bNums.isNotEmpty()) {
                val cmp = aNums[0].compareTo(bNums[0])
                if (cmp != 0) return@Comparator cmp
            }

            // Fallback to normal string comparison
            return@Comparator a.compareTo(b, ignoreCase = true)
        }
    }



    fun loadSavedCourses(db: AppDatabase, context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val savedCourses = db.courseDao().getAllCourses()
            if (savedCourses.isNotEmpty()) {
                val coursesWithVideos = savedCourses.map { c ->
                    val videos = db.videoDao().getVideosForCourse(c.id).map { v ->
                        Video(v.id, Uri.parse(v.uri), v.isComplete)
                    }
                    Course(c.id, c.name, c.thumbnailUri, videos)
                }

                withContext(Dispatchers.Main) {
                    setCourses(coursesWithVideos)
                }
            }
        }
    }
}

/** --------------------- MAIN ACTIVITY --------------------- */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            Course_trackerTheme {
                val navController = rememberNavController()
                val viewModel: CourseViewModel = viewModel()
                val context = LocalContext.current
                val db = AppDatabase.getDatabase(context)

                LaunchedEffect(Unit) { viewModel.loadSavedCourses(db, context) }

                // In MainActivity.kt -> onCreate

                NavHost(navController, startDestination = "course_list") {
                    composable("course_list") {
                        CourseListScreen(viewModel, db) { course ->
                            // You don't need to pass the courseId via SavedStateHandle if you're navigating directly
                            navController.navigate("video_list/${course.id}")
                        }
                    }
                    composable(
                        "video_list/{courseId}",
                        arguments = listOf(navArgument("courseId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val courseId =
                            backStackEntry.arguments?.getString("courseId") ?: return@composable
                        val course = viewModel.getCourse(courseId) ?: return@composable
                        VideoListScreen(
                            course = course,
                            viewModel = viewModel,
                            db = db,
                            onNavigateUp = { navController.navigateUp() } // Pass the navigate up action
                        )
                    }
                }

            }
        }
    }

    /** --------------------- COMPOSABLES --------------------- */
// In MainActivity.kt

    @Composable
    fun CourseListScreen(
        viewModel: CourseViewModel,
        db: AppDatabase,
        onCourseClick: (Course) -> Unit
    ) {
        val context = LocalContext.current
        val courses = viewModel.courses
        val scope = rememberCoroutineScope()

        val folderPickerLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.OpenDocumentTree()
        ) { uri: Uri? ->
            uri?.let {
                context.contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                // Use the activity context to launch the coroutine
                (context as? Activity)?.let { activity ->
                    scope.launch { viewModel.loadCoursesFromParentFolder(activity, it, db) }
                }
            }
        }

        Scaffold(
            // Apply modifier to respect system gestures (like the back gesture)
            modifier = Modifier.fillMaxSize(),
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { folderPickerLauncher.launch(null) },
                    // Apply insets to the FAB to avoid the navigation bar
                    modifier = Modifier.windowInsetsPadding(
                        WindowInsets.safeDrawing.only(
                            WindowInsetsSides.Bottom
                        )
                    )
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add course")
                }
            }
        ) { innerPadding ->
            LazyColumn(
                // Use the innerPadding from the Scaffold
                modifier = Modifier.padding(innerPadding),
                // Add content padding to respect the status and navigation bars
                contentPadding = WindowInsets.safeDrawing
                    .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top)
                    .asPaddingValues()
            ) {
                items(courses) { course ->
                    CourseCard(course, viewModel, context) { onCourseClick(course) }
                }
            }
        }
    }


    // In MainActivity.kt

    @Composable
    fun CourseCard(course: Course, viewModel: CourseViewModel, context: Context, onClick: () -> Unit) {
        val thumbnail by course.thumbnailState
        LaunchedEffect(course.id) { viewModel.loadCourseThumbnail(course, context) }

        val completedCount = course.videos.count { it.isComplete }
        val totalCount = course.videos.size
        val completionPercent = if (totalCount > 0) completedCount.toFloat() / totalCount else 0f

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp) // Adjust padding
                .clickable(onClick = onClick),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    AsyncImage(
                        model = thumbnail,
                        contentDescription = "Course Thumbnail",
                        modifier = Modifier
                            .size(80.dp) // Slightly smaller image
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Inside, // Ensure image fills the space
                        placeholder = painterResource(id = R.drawable.ic_launcher_background), // Add a placeholder
                        error = painterResource(id = R.drawable.ic_launcher_background) // Add an error drawable
                    )
                    Spacer(Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(course.name, style = MaterialTheme.typography.titleLarge)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "${(completionPercent * 100).toInt()}% completed ($completedCount/$totalCount)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
                LinearProgressIndicator(
                    progress = { completionPercent }, // For Material 3
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    strokeCap = StrokeCap.Round // Nicer rounded caps
                )
            }
        }
    }


// In MainActivity.kt

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun VideoListScreen(
        course: Course,
        viewModel: CourseViewModel,
        db: AppDatabase,
        onNavigateUp: () -> Unit
    ) {
        val scope = rememberCoroutineScope()

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(course.name, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    navigationIcon = {
                        IconButton(onClick = onNavigateUp) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    // Apply insets to the TopAppBar
                    modifier = Modifier.windowInsetsPadding(
                        WindowInsets.safeDrawing.only(
                            WindowInsetsSides.Top
                        )
                    )
                )
            }
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding), // Use padding from Scaffold
                // Add horizontal and bottom padding to avoid system bars and FAB area
                contentPadding = WindowInsets.safeDrawing
                    .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom)
                    .asPaddingValues()
            ) {
                items(course.videos) { video ->
                    VideoItem(
                        video = video,
                        onToggleComplete = {
                            val newStatus = !video.isComplete
                            viewModel.updateVideo(course.id, video.id, newStatus)
                            scope.launch(Dispatchers.IO) {
                                db.videoDao().updateVideo(
                                    VideoEntity(
                                        video.id,
                                        course.id,
                                        video.uri.toString(),
                                        newStatus
                                    )
                                )
                            }
                        },
                        context = LocalContext.current
                    )
                }
            }
        }
    }


    @Composable
    fun VideoItem(video: Video, onToggleComplete: () -> Unit, context: Context) {
        val thumbnailState = remember(video.uri) { mutableStateOf<String?>(null) }

        // Generate thumbnail asynchronously
        LaunchedEffect(video.uri) {
            thumbnailState.value = withContext(Dispatchers.IO) {
                try {
                    val retriever = MediaMetadataRetriever()
                    retriever.setDataSource(context, video.uri)
                    val bitmap = retriever.frameAtTime
                    retriever.release()
                    bitmap?.let {
                        val file = File(context.cacheDir, "${video.id}_thumb.png")
                        FileOutputStream(file).use { out ->
                            it.compress(
                                Bitmap.CompressFormat.PNG,
                                100,
                                out
                            )
                        }
                        file.absolutePath
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .clickable {
                    // Open external video player
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(video.uri, "video/*")
                        flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                    }
                    context.startActivity(intent)
                }
        ) {
            Row(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = thumbnailState.value,
                    contentDescription = "Video Thumbnail",
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
                Spacer(Modifier.width(16.dp))
                Text(
                    text = video.uri.lastPathSegment?.substringAfterLast('/')
                        ?.substringBeforeLast('.') ?: "Video",
                    modifier = Modifier.weight(1f)
                )
                Checkbox(
                    checked = video.isComplete,
                    onCheckedChange = { onToggleComplete() },
                    colors = CheckboxDefaults.colors(
                        checkedColor = MaterialTheme.colorScheme.primary,   // when checked
                        uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant, // when not checked
                        checkmarkColor = MaterialTheme.colorScheme.onPrimary // color of ✓
                    )
                )

            }
        }
    }
}