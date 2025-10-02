package com.prateektimer.course_tracker.data.repository

import android.app.Activity
import android.content.Context
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
