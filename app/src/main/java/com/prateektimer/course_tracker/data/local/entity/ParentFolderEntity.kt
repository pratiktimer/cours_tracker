package com.prateektimer.course_tracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "parent_folder")
data class ParentFolderEntity(
    @PrimaryKey val id: Int = 0,
    val uri: String
)
