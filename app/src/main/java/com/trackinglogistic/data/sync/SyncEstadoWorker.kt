package com.trackinglogistic.data.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.trackinglogistic.domain.repository.PaqueteRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/** Envía con PUT /api/tracking/{guia}/estado los cambios guardados con syncPendiente = true. */
@HiltWorker
class SyncEstadoWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val repository: PaqueteRepository,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result =
        if (repository.sincronizarPendientes()) Result.success() else Result.retry()

    companion object {
        const val NOMBRE = "sync-estados"
    }
}
