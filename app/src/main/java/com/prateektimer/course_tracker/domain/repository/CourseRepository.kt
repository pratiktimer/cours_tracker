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
