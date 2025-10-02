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
