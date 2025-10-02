package com.prateektimer.course_tracker.domain.usecase

import android.app.Activity
import android.net.Uri
import com.prateektimer.course_tracker.domain.repository.CourseRepository
import javax.inject.Inject

class LoadCoursesFromFolderUseCase @Inject constructor(
    private val repository: CourseRepository
) {
    suspend operator fun invoke(activity: Activity, folderUri: Uri) {
        repository.loadCoursesFromFolder(activity, folderUri)
    }
}
