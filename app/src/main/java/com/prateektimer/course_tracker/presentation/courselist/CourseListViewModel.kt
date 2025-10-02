package com.prateektimer.course_tracker.presentation.courselist

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prateektimer.course_tracker.domain.model.Course
import com.prateektimer.course_tracker.domain.usecase.GetCoursesUseCase
import com.prateektimer.course_tracker.domain.usecase.LoadCoursesFromFolderUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
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
    fun loadCourseThumbnail(course: Course, context: Context) {
        if (course.thumbnailState.value != null || course.videos.isEmpty()) return
        val firstVideo = course.videos[0]

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val retriever = MediaMetadataRetriever()
                retriever.setDataSource(context, firstVideo.uri)
                val bitmap = retriever.frameAtTime
                retriever.release()

                bitmap?.let {
                    val file = File(context.cacheDir, "${course.id}_thumb.png")
                    FileOutputStream(file).use { out ->
                        it.compress(Bitmap.CompressFormat.PNG, 100, out)
                    }
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

}
