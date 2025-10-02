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
