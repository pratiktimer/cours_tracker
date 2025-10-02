package com.prateektimer.course_tracker.domain.usecase

import com.prateektimer.course_tracker.domain.repository.CourseRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class UpdateVideoCompletionUseCaseTest {

    private lateinit var repository: CourseRepository
    private lateinit var useCase: UpdateVideoCompletionUseCase

    @Before
    fun setup() {
        repository = mockk()
        useCase = UpdateVideoCompletionUseCase(repository)
    }

    @Test
    fun `invoke calls repository updateVideoCompletion`() = runTest {
        // Given
        val courseId = "course1"
        val videoId = "video1"
        val isComplete = true
        coEvery { repository.updateVideoCompletion(any(), any(), any()) } returns Unit

        // When
        useCase(courseId, videoId, isComplete)

        // Then
        coVerify(exactly = 1) { 
            repository.updateVideoCompletion(courseId, videoId, isComplete) 
        }
    }
}
