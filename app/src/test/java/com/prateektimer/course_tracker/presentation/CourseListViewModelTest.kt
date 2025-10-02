package com.prateektimer.course_tracker.presentation

import android.net.Uri
import app.cash.turbine.test
import com.prateektimer.course_tracker.domain.model.Course
import com.prateektimer.course_tracker.domain.model.Video
import com.prateektimer.course_tracker.domain.usecase.GetCoursesUseCase
import com.prateektimer.course_tracker.domain.usecase.LoadCoursesFromFolderUseCase
import com.prateektimer.course_tracker.presentation.courselist.CourseListViewModel
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

@OptIn(ExperimentalCoroutinesApi::class)
class CourseListViewModelTest {

    private lateinit var getCoursesUseCase: GetCoursesUseCase
    private lateinit var loadCoursesFromFolderUseCase: LoadCoursesFromFolderUseCase
    private lateinit var viewModel: CourseListViewModel

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        getCoursesUseCase = mockk()
        loadCoursesFromFolderUseCase = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is empty`() = runTest {
        // Given
        every { getCoursesUseCase() } returns flowOf(emptyList())

        // When
        viewModel = CourseListViewModel(getCoursesUseCase, loadCoursesFromFolderUseCase)

        // Then
        viewModel.state.test {
            val emission = awaitItem()
            assertEquals(emptyList(), emission.courses)
            assertFalse(emission.isLoading)
        }
    }

    @Test
    fun `courses are loaded from use case`() = runTest {
        // Given
        val mockUri = mockk<Uri>()
        val courses = listOf(
            Course("1", "Course 1", null, listOf(Video("v1", mockUri, false)))
        )
        every { getCoursesUseCase() } returns flowOf(courses)

        // When
        viewModel = CourseListViewModel(getCoursesUseCase, loadCoursesFromFolderUseCase)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        viewModel.state.test {
            val emission = awaitItem()
            assertEquals(courses, emission.courses)
        }
    }
}
