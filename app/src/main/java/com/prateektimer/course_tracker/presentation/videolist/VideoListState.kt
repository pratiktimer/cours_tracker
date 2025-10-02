package com.prateektimer.course_tracker.presentation.videolist

import com.prateektimer.course_tracker.domain.model.Course

data class VideoListState(
    val course: Course? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)
