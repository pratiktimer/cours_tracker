package com.prateektimer.course_tracker.data.local.dao

import androidx.room.*
import com.prateektimer.course_tracker.data.local.entity.ParentFolderEntity

@Dao
interface ParentFolderDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(folder: ParentFolderEntity)
    
    @Query("SELECT * FROM parent_folder LIMIT 1")
    suspend fun getFolder(): ParentFolderEntity?
}
