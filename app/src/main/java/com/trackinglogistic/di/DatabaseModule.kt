package com.trackinglogistic.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.room.Room
import androidx.work.WorkManager
import com.trackinglogistic.data.local.AppDatabase
import com.trackinglogistic.data.local.PaqueteDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Único punto de la app donde se construyen la base de datos Room y el DataStore.
 * @Singleton: una sola conexión a SQLite (evita bloqueos "database is locked" e
 * invalidaciones de Flow perdidas entre instancias) y un solo DataStore por archivo,
 * que es obligatorio: dos instancias sobre el mismo archivo lanzan IllegalStateException.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.NOMBRE).build()

    @Provides
    @Singleton
    fun providePaqueteDao(database: AppDatabase): PaqueteDao = database.paqueteDao()

    @Provides
    @Singleton
    fun providePreferencesDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        PreferenceDataStoreFactory.create { context.preferencesDataStoreFile("preferencias") }

    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager =
        WorkManager.getInstance(context)
}
