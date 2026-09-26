package com.trackinglogistic.data.sync

import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

interface SyncScheduler {
    fun programarSincronizacion()
}

@Singleton
class WorkManagerSyncScheduler @Inject constructor(
    private val workManager: WorkManager,
) : SyncScheduler {

    override fun programarSincronizacion() {
        val request = OneTimeWorkRequestBuilder<SyncEstadoWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build(),
            )
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 15, TimeUnit.SECONDS)
            .build()
        // APPEND_OR_REPLACE: si hay una sincronización en curso, esta se encadena detrás
        // para no perder cambios hechos mientras la anterior se ejecutaba.
        workManager.enqueueUniqueWork(SyncEstadoWorker.NOMBRE, ExistingWorkPolicy.APPEND_OR_REPLACE, request)
    }
}
