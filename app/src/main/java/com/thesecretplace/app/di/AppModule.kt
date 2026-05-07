package com.thesecretplace.app.di

import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import com.thesecretplace.app.data.local.AppDatabase
import com.thesecretplace.app.data.local.JournalDao
import com.thesecretplace.app.data.PreferencesManager
import com.thesecretplace.app.service.AudioServiceConnection
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences("the_secret_place_prefs", Context.MODE_PRIVATE)
    }

    @Provides
    @Singleton
    fun providePreferencesManager(prefs: SharedPreferences): PreferencesManager {
        return PreferencesManager(prefs)
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "meditation_database"
        ).build()
    }

    @Provides
    @Singleton
    fun provideJournalDao(database: AppDatabase): JournalDao {
        return database.journalDao()
    }

    @Provides
    @Singleton
    fun provideAudioServiceConnection(@ApplicationContext context: Context): AudioServiceConnection {
        return AudioServiceConnection(context)
    }
}
