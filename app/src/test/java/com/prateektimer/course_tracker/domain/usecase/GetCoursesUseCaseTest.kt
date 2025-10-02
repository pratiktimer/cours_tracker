package com.prateektimer.course_tracker.domain.usecase

import android.net.Uri
import com.prateektimer.course_tracker.domain.model.Course
import com.prateektimer.course_tracker.domain.model.Video
import com.prateektimer.course_tracker.domain.repository.CourseRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class GetCoursesUseCaseTest {

    private lateinit var repository: CourseRepository
    private lateinit var useCase: GetCoursesUseCase

    @Before
    fun setup() {
        repository = mockk()
        useCase = GetCoursesUseCase(repository)
    }

    @Test
    fun `invoke returns courses from repository`() = runTest {
        // Given
        val mockUri = mockk<Uri>()
        val expectedCourses = listOf(
            Course(
                id = "1",
                name = "Course 1",
                videos = listOf(
                    Video("v1", mockUri, false)
                )
            )
        )
        every { repository.getAllCourses() } returns flowOf(expectedCourses)

        // When
        val result = useCase().toList()

        // Then
        assertEquals(1, result.size)
        assertEquals(expectedCourses, result[0])
    }

    @Test
    fun `invoke returns empty list when no courses`() = runTest {
        // Given
        every { repository.getAllCourses() } returns flowOf(emptyList())

        // When
        val result = useCase().toList()

        // Then
        assertEquals(1, result.size)
        assertEquals(emptyList(), result[0])
    }
}
