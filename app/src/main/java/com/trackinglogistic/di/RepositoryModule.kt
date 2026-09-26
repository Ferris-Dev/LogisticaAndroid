package com.trackinglogistic.di

import com.trackinglogistic.data.preferences.PilotoPreferences
import com.trackinglogistic.data.repository.PaqueteRepositoryImpl
import com.trackinglogistic.data.sync.SyncScheduler
import com.trackinglogistic.data.sync.WorkManagerSyncScheduler
import com.trackinglogistic.domain.repository.PaqueteRepository
import com.trackinglogistic.domain.repository.PilotoRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** Enlaza las interfaces del dominio con sus implementaciones de la capa data. */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindPaqueteRepository(impl: PaqueteRepositoryImpl): PaqueteRepository

    @Binds
    @Singleton
    abstract fun bindPilotoRepository(impl: PilotoPreferences): PilotoRepository

    @Binds
    @Singleton
    abstract fun bindSyncScheduler(impl: WorkManagerSyncScheduler): SyncScheduler
}
