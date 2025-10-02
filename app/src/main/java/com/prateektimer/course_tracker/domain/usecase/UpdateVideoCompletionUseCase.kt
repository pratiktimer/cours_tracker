package com.prateektimer.course_tracker.domain.usecase

import com.prateektimer.course_tracker.domain.repository.CourseRepository
import javax.inject.Inject

class UpdateVideoCompletionUseCase @Inject constructor(
    private val repository: CourseRepository
) {
    suspend operator fun invoke(courseId: String, videoId: String, isComplete: Boolean) {
        repository.updateVideoCompletion(courseId, videoId, isComplete)
    }
}
