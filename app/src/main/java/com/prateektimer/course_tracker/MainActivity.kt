package com.prateektimer.course_tracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.prateektimer.course_tracker.presentation.courselist.CourseListScreen
import com.prateektimer.course_tracker.presentation.courselist.CourseListViewModel
import com.prateektimer.course_tracker.presentation.videolist.VideoListScreen
import com.prateektimer.course_tracker.presentation.videolist.VideoListViewModel
import com.prateektimer.course_tracker.ui.theme.Course_trackerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()


        setContent {
            Course_trackerTheme {
                val navController = rememberNavController()

                NavHost(navController, startDestination = "course_list") {
                    composable("course_list") {
                        val courseListViewModel: CourseListViewModel = hiltViewModel()
                        CourseListScreen(
                            onCourseClick = { course ->
                                navController.navigate("video_list/${course.id}")
                            },
                            viewModel = courseListViewModel
                        )
                    }
                    composable(
                        "video_list/{courseId}",
                        arguments = listOf(navArgument("courseId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val viewModel: VideoListViewModel = hiltViewModel(backStackEntry)
                        VideoListScreen(
                            onNavigateUp = { navController.navigateUp() },
                            viewModel = viewModel
                        )
                    }
                }

            }
        }
    }
}
