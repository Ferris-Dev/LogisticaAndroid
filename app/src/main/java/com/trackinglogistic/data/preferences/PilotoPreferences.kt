package com.trackinglogistic.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.trackinglogistic.domain.repository.PilotoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PilotoPreferences @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : PilotoRepository {

    override val pilotoId: Flow<String?> = dataStore.data.map { it[KEY_PILOTO] }

    override suspend fun guardar(pilotoId: String) {
        dataStore.edit { it[KEY_PILOTO] = pilotoId }
    }

    override suspend fun cerrarSesion() {
        dataStore.edit { it.remove(KEY_PILOTO) }
    }

    private companion object {
        val KEY_PILOTO = stringPreferencesKey("piloto_id")
    }
}
