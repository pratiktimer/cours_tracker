package com.prateektimer.course_tracker.data.local.dao

import androidx.room.*
import com.prateektimer.course_tracker.data.local.entity.VideoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VideoDao {
    @Query("SELECT * FROM videos WHERE courseId = :courseId")
    suspend fun getVideosForCourse(courseId: String): List<VideoEntity>
    
    @Query("SELECT * FROM videos WHERE courseId = :courseId")
    fun getVideosForCourseFlow(courseId: String): Flow<List<VideoEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideos(videos: List<VideoEntity>)
    
    @Update
    suspend fun updateVideo(video: VideoEntity)
    
    @Query("UPDATE videos SET isComplete = :isComplete WHERE id = :videoId")
    suspend fun updateVideoCompletion(videoId: String, isComplete: Boolean)
}
