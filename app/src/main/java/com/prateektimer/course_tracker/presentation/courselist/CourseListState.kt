package com.prateektimer.course_tracker.presentation.courselist

import com.prateektimer.course_tracker.domain.model.Course

data class CourseListState(
    val courses: List<Course> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
