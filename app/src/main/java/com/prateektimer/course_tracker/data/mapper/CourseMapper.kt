package com.prateektimer.course_tracker.data.mapper

import android.net.Uri
import com.prateektimer.course_tracker.data.local.entity.CourseEntity
import com.prateektimer.course_tracker.data.local.entity.VideoEntity
import com.prateektimer.course_tracker.domain.model.Course
import com.prateektimer.course_tracker.domain.model.Video

fun CourseEntity.toDomain(videos: List<Video>): Course {
    return Course(
        id = id,
        name = name,
        thumbnailPath = thumbnailUri,
        videos = videos
    )
}

fun Course.toEntity(): CourseEntity {
    return CourseEntity(
        id = id,
        name = name,
        thumbnailUri = thumbnailPath
    )
}

fun VideoEntity.toDomain(): Video {
    return Video(
        id = id,
        uri = Uri.parse(uri),
        isComplete = isComplete
    )
}

fun Video.toEntity(courseId: String): VideoEntity {
    return VideoEntity(
        id = id,
        courseId = courseId,
        uri = uri.toString(),
        isComplete = isComplete
    )
}
