package com.prateektimer.course_tracker.di

import android.content.Context
import androidx.room.Room
import com.prateektimer.course_tracker.data.local.AppDatabase
import com.prateektimer.course_tracker.data.local.dao.CourseDao
import com.prateektimer.course_tracker.data.local.dao.ParentFolderDao
import com.prateektimer.course_tracker.data.local.dao.VideoDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "course_db"
        ).build()
    }

    @Provides
    fun provideCourseDao(database: AppDatabase): CourseDao {
        return database.courseDao()
    }

    @Provides
    fun provideVideoDao(database: AppDatabase): VideoDao {
        return database.videoDao()
    }

    @Provides
    fun provideParentFolderDao(database: AppDatabase): ParentFolderDao {
        return database.parentFolderDao()
    }
}
