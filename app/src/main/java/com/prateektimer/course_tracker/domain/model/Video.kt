package com.prateektimer.course_tracker.domain.model

import android.net.Uri

data class Video(
    val id: String,
    val uri: Uri,
    val isComplete: Boolean = false
)
