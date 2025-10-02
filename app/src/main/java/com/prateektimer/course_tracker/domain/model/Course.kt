package com.prateektimer.course_tracker.domain.model

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf

data class Course(
    val id: String,
    val name: String,
    var thumbnailPath: String? = null,
    val thumbnailState: MutableState<String?> = mutableStateOf(null),
    val videos: List<Video> = emptyList()
) {
    val completionPercentage: Float
        get() = if (videos.isEmpty()) 0f 
                else videos.count { it.isComplete }.toFloat() / videos.size
    
    val completedCount: Int
        get() = videos.count { it.isComplete }
}
