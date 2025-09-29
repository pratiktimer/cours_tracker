package com.prateektimer.course_tracker

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Checkbox
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.ViewModel
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
        @Volatile private var INSTANCE: AppDatabase? = null

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
    var isComplete: Boolean = false,
)

data class Course(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val thumbnailPath: String? = null, // Path to saved thumbnail
    val videos: List<Video>
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

                NavHost(navController, startDestination = "course_list") {
                    composable("course_list") {
                        CourseListScreen(
                            viewModel = viewModel,
                            onCourseClick = { course ->
                                navController.currentBackStackEntry?.savedStateHandle
                                    ?.set("courseId", course.id)
                                navController.navigate("video_list/${course.id}")
                            },
                            db = db
                        )
                    }
                    composable(
                        "video_list/{courseId}",
                        arguments = listOf(navArgument("courseId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val courseId = backStackEntry.arguments?.getString("courseId") ?: return@composable
                        val course = viewModel.getCourse(courseId) ?: return@composable
                        VideoListScreen(
                            course = course,
                            viewModel = viewModel,
                            db = db
                        )
                    }
                }
            }
        }
    }
}

/** --------------------- COMPOSABLES --------------------- */
@Composable
fun CourseListScreen(viewModel: CourseViewModel, onCourseClick: (Course) -> Unit, db: AppDatabase) {
    val context = LocalContext.current
    val parentFolderDao = db.parentFolderDao()
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

            scope.launch {
                parentFolderDao.insert(ParentFolderEntity(uri = it.toString()))
                val loadedCourses = loadCoursesFromParentFolder(context as Activity, it)

                // Save to Room
                val courseEntities = loadedCourses.map { c -> CourseEntity(c.id, c.name, thumbnailUri = c.thumbnailPath) }
                val videoEntities = loadedCourses.flatMap { c ->
                    c.videos.map { v -> VideoEntity(v.id, c.id, v.uri.toString(), v.isComplete) }
                }
                db.courseDao().insertCourses(courseEntities)
                db.videoDao().insertVideos(videoEntities)

                viewModel.setCourses(loadedCourses)
            }
        }
    }

    LaunchedEffect(Unit) {
        val savedCourses = db.courseDao().getAllCourses()
        if (savedCourses.isNotEmpty()) {
            val coursesWithVideos = savedCourses.map { c ->
                val videos = db.videoDao().getVideosForCourse(c.id).map { v ->
                    Video(v.id, Uri.parse(v.uri), v.isComplete)
                }
                Course(c.id, c.name, c.thumbnailUri, videos)
            }
            viewModel.setCourses(coursesWithVideos)
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { folderPickerLauncher.launch(null) }) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding)) {
            items(courses) { course ->
                CourseCard(course) { onCourseClick(course) }
            }
        }
    }
}

@Composable
fun CourseCard(course: Course, onClick: () -> Unit) {

    val completedCount = course.videos.count { it.isComplete }
    val totalCount = course.videos.size
    val completionPercent = if (totalCount > 0) completedCount.toFloat() / totalCount else 0f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = course.thumbnailPath, // or course.thumbnailPath
                    contentDescription = "Video Thumbnail",
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(course.name, style = MaterialTheme.typography.titleMedium)
                    Text(
                        "${(completionPercent * 100).toInt()}% completed",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { completionPercent },
                modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                color = ProgressIndicatorDefaults.linearColor,
                trackColor = ProgressIndicatorDefaults.linearTrackColor,
                strokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
            )
        }
    }
}


@Composable
fun VideoListScreen(course: Course, viewModel: CourseViewModel, db: AppDatabase) {
    val scope = rememberCoroutineScope()
    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        items(course.videos) { video ->
            VideoItem(video = video, onToggleComplete = {
                val newStatus = !video.isComplete
                viewModel.updateVideo(course.id, video.id, newStatus)
                scope.launch {
                    db.videoDao().updateVideo(VideoEntity(video.id, course.id, video.uri.toString(), newStatus))
                }
            }, context = LocalContext.current)
        }
    }
}

@Composable
fun VideoItem(video: Video, onToggleComplete: () -> Unit, context: Context) {
    // Determine thumbnail path
    val thumbnailPath = remember(video.uri) { getVideoThumbnail(context as Activity, video.uri) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = thumbnailPath, // Coil will load from file if exists
                contentDescription = "Video Thumbnail",
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp))
            )

            Spacer(Modifier.width(16.dp))

            Text(
                text = video.uri.lastPathSegment?.substringAfterLast('/')?.substringBeforeLast('.') ?: "Video",
                modifier = Modifier.weight(1f)
            )

            Checkbox(
                checked = video.isComplete,
                onCheckedChange = { onToggleComplete() }
            )
        }
    }
}


/** --------------------- HELPERS --------------------- */
fun loadCoursesFromParentFolder(activity: Activity, parentUri: Uri): List<Course> {
    val parent = DocumentFile.fromTreeUri(activity, parentUri) ?: return emptyList()
    val courses = mutableListOf<Course>()

    parent.listFiles().forEach { subFolder ->
        if (subFolder.isDirectory) {
            val videoFiles = subFolder.listFiles()
                .filter { it.isFile && it.name?.endsWith(".mp4", true) == true }
                .map { Video(uri = it.uri, isComplete = false) }

            if (videoFiles.isNotEmpty()) {
                val thumbnailBitmap = getVideoThumbnail(activity, videoFiles[1].uri)
                val thumbnailPath = thumbnailBitmap?.let { saveThumbnailToCache(activity, it, subFolder.name ?: UUID.randomUUID().toString()) }

                courses.add(
                    Course(
                        id = UUID.randomUUID().toString(),
                        name = subFolder.name ?: "Untitled",
                        thumbnailPath = thumbnailPath,
                        videos = videoFiles
                    )
                )
            }
        }
    }

    return courses
}


data class CourseWithVideos(
    val course: Course,
    val videos: List<Video>
)
suspend fun generateAndSaveVideoThumbnail(
    context: Context,
    videoPath: String
): String? = withContext(Dispatchers.IO) {
    try {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(videoPath)

        val bitmap = retriever.getFrameAtTime(0) // first frame
        retriever.release()

        bitmap?.let {
            val thumbnailsDir = File(context.filesDir, "video_thumbnails")
            if (!thumbnailsDir.exists()) thumbnailsDir.mkdirs()

            val id = videoPath.hashCode().toString()
            val thumbnailFile = File(thumbnailsDir, "$id.jpg")

            FileOutputStream(thumbnailFile).use { out ->
                it.compress(Bitmap.CompressFormat.JPEG, 80, out)
            }
            return@withContext thumbnailFile.absolutePath
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    null
}


fun saveThumbnailToCache(context: Context, bitmap: Bitmap, courseId: String): String {
    val file = File(context.cacheDir, "${courseId}_thumb.png")
    file.outputStream().use { out ->
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
    }
    return file.absolutePath
}


fun getVideoThumbnail(activity: Activity, videoUri: Uri): Bitmap? {
    return try {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(activity, videoUri)
        val bitmap = retriever.frameAtTime
        retriever.release()
        bitmap
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
