package com.prateektimer.course_tracker.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.prateektimer.course_tracker.data.local.dao.CourseDao
import com.prateektimer.course_tracker.data.local.dao.ParentFolderDao
import com.prateektimer.course_tracker.data.local.dao.VideoDao
import com.prateektimer.course_tracker.data.local.entity.CourseEntity
import com.prateektimer.course_tracker.data.local.entity.ParentFolderEntity
import com.prateektimer.course_tracker.data.local.entity.VideoEntity

@Database(
    entities = [CourseEntity::class, VideoEntity::class, ParentFolderEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun courseDao(): CourseDao
    abstract fun videoDao(): VideoDao
    abstract fun parentFolderDao(): ParentFolderDao
}
