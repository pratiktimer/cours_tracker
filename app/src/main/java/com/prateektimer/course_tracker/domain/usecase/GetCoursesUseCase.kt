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
