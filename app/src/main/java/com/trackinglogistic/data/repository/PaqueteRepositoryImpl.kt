package com.trackinglogistic.data.repository

import com.trackinglogistic.data.local.PaqueteDao
import com.trackinglogistic.data.local.PaqueteEntity
import com.trackinglogistic.data.mapper.toDomain
import com.trackinglogistic.data.mapper.toEntity
import com.trackinglogistic.data.remote.TrackingApi
import com.trackinglogistic.data.remote.dto.ActualizarEstadoRequest
import com.trackinglogistic.data.remote.dto.PaqueteDetalleDto
import com.trackinglogistic.data.remote.errorEnCampo
import com.trackinglogistic.data.remote.mensajeDeError
import com.trackinglogistic.data.remote.problemDetails
import com.trackinglogistic.data.sync.SyncScheduler
import com.trackinglogistic.di.IoDispatcher
import com.trackinglogistic.domain.model.CodigoPiloto
import com.trackinglogistic.domain.model.EstadoPaquete
import com.trackinglogistic.domain.model.Paquete
import com.trackinglogistic.domain.model.ResultadoBusqueda
import com.trackinglogistic.domain.model.ResultadoRefresco
import com.trackinglogistic.domain.repository.PaqueteRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PaqueteRepositoryImpl @Inject constructor(
    private val api: TrackingApi,
    private val dao: PaqueteDao,
    private val syncScheduler: SyncScheduler,
    private val json: Json,
    @param:IoDispatcher private val io: CoroutineDispatcher,
) : PaqueteRepository {

    override fun observarPaquetes(pilotoId: String): Flow<List<Paquete>> =
        dao.observarPorPiloto(pilotoId).map { lista -> lista.map { it.toDomain() } }

    override fun observarPaquete(guia: String): Flow<Paquete?> =
        dao.observar(guia).map { it?.toDomain() }

    override suspend fun refrescar(pilotoId: String): ResultadoRefresco = withContext(io) {
        val idNumerico = CodigoPiloto.idNumerico(pilotoId)
            ?: return@withContext ResultadoRefresco.Error("Código de piloto inválido: $pilotoId")
        try {
            val resumenes = api.obtenerPaquetes(idNumerico).map { it.toEntity(pilotoId) }
            dao.reemplazarDelPiloto(pilotoId, resumenes)
            descargarDetallesFaltantes(pilotoId)
            ResultadoRefresco.Exito
        } catch (e: CancellationException) {
            throw e
        } catch (e: IOException) {
            ResultadoRefresco.SinConexion
        } catch (e: HttpException) {
            val mensaje = mensajeDeError(e.code(), e.problemDetails(json))
            // 404 = el piloto no existe; distinto de 200 [] (sin entregas hoy).
            if (e.code() == 404) ResultadoRefresco.PilotoNoExiste(mensaje) else ResultadoRefresco.Error(mensaje)
        } catch (e: Exception) {
            ResultadoRefresco.Error("No se pudo leer la respuesta del servidor")
        }
    }

    /**
     * La lista solo trae resúmenes. Se descarga el detalle (teléfono, dirección) de los
     * paquetes que aún no lo tienen, para que el piloto pueda verlos después sin conexión.
     * Es best-effort: si alguno falla, se reintentará al abrir su detalle o en el próximo refresco.
     */
    private suspend fun descargarDetallesFaltantes(pilotoId: String) = coroutineScope {
        val limite = Semaphore(MAX_DESCARGAS_PARALELAS)
        dao.guiasSinDetalle(pilotoId).map { guia ->
            async { limite.withPermit { refrescarDetalle(guia) } }
        }.awaitAll()
    }

    override suspend fun refrescarDetalle(guia: String): ResultadoRefresco = withContext(io) {
        try {
            val respuesta = api.obtenerDetalle(guia)
            val dto = respuesta.body()
            if (respuesta.isSuccessful && dto != null) {
                dao.guardarDetalle(dto.toEntity())
                ResultadoRefresco.Exito
            } else {
                ResultadoRefresco.Error(mensajeDeError(respuesta.code(), respuesta.problemDetails(json)))
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: IOException) {
            ResultadoRefresco.SinConexion
        } catch (e: Exception) {
            ResultadoRefresco.Error("No se pudo leer la respuesta del servidor")
        }
    }

    override suspend fun buscarRemoto(guia: String): ResultadoBusqueda = withContext(io) {
        try {
            val respuesta = api.obtenerDetalle(guia)
            val dto = respuesta.body()
            when {
                respuesta.isSuccessful && dto != null -> {
                    // Se guarda en Room porque el detalle siempre lee de Room. Si la guía ya
                    // existía se conserva su piloto y cualquier cambio pendiente.
                    dao.guardarDetalle(dto.toEntity())
                    ResultadoBusqueda.Encontrado(dto.toEntity().toDomain())
                }
                else -> {
                    val mensaje = mensajeDeError(respuesta.code(), respuesta.problemDetails(json))
                    when (respuesta.code()) {
                        404 -> ResultadoBusqueda.NoExiste(mensaje)
                        400 -> ResultadoBusqueda.FormatoInvalido(mensaje)
                        else -> ResultadoBusqueda.Error(mensaje)
                    }
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: IOException) {
            ResultadoBusqueda.SinConexion
        } catch (e: Exception) {
            ResultadoBusqueda.Error("No se pudo leer la respuesta del servidor")
        }
    }

    override suspend fun actualizarEstado(guia: String, estado: EstadoPaquete, observaciones: String?) {
        // La fecha se toma ahora (momento real del cambio en el dispositivo), no cuando el
        // Worker logre enviarlo: así el last-write-wins del servidor ordena bien los cambios offline.
        val fechaCambio = Instant.now().truncatedTo(ChronoUnit.SECONDS).toString()
        withContext(io) { dao.actualizarEstadoLocal(guia, estado.name, fechaCambio, observaciones) }
        syncScheduler.programarSincronizacion()
    }

    /**
     * Envía la cola en orden cronológico. Reglas del servidor (last-write-wins):
     * - El PUT responde 200 con el estado VIGENTE aunque no haya aplicado el cambio.
     * - Si la respuesta difiere de lo enviado, un cambio más reciente lo superó:
     *   se descarta de la cola (no se reintenta) y se avisa al piloto.
     * - Reenviar el mismo cambio es idempotente, así que reintentar tras un fallo de red es seguro.
     */
    override suspend fun sincronizarPendientes(): Boolean = withContext(io) {
        var reintentar = false
        for (paquete in dao.pendientes()) {
            try {
                // errorBody() solo se puede leer una vez: el ProblemDetails se guarda al recibirlo.
                var respuesta = enviar(paquete, conFecha = true)
                var problema = if (respuesta.isSuccessful) null else respuesta.problemDetails(json)
                if (respuesta.code() == 400 && problema?.errorEnCampo("fechaCambio") == true) {
                    // Reloj del dispositivo adelantado (> 5 min): se reenvía sin fecha y el
                    // servidor usa su propia hora, en lugar de perder el cambio.
                    respuesta = enviar(paquete, conFecha = false)
                    problema = if (respuesta.isSuccessful) null else respuesta.problemDetails(json)
                }
                when {
                    respuesta.isSuccessful -> aplicarRespuesta(paquete, respuesta.body())
                    // 5xx: error temporal del servidor, se reintenta más tarde.
                    respuesta.code() >= 500 -> reintentar = true
                    // 400/404: el servidor rechaza el cambio y reintentar no lo arreglará.
                    else -> rechazar(paquete, mensajeDeError(respuesta.code(), problema))
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: IOException) {
                reintentar = true
            }
        }
        !reintentar
    }

    private suspend fun enviar(paquete: PaqueteEntity, conFecha: Boolean): Response<PaqueteDetalleDto> =
        api.actualizarEstado(
            paquete.guia,
            ActualizarEstadoRequest(
                estado = paquete.estado,
                fechaCambio = if (conFecha) paquete.fechaCambio else null,
                observaciones = paquete.observacionesPendientes,
            ),
        )

    private suspend fun aplicarRespuesta(enviado: PaqueteEntity, vigente: PaqueteDetalleDto?) {
        val salioDeLaCola = dao.resolverSync(enviado.guia, enviado.fechaCambio) > 0
        // La UI muestra siempre el estado de la respuesta del servidor, no el que se envió.
        // (Si mientras tanto el piloto hizo otro cambio, guardarDetalle conserva ese cambio local.)
        vigente?.let { dao.guardarDetalle(it.toEntity()) }
        if (salioDeLaCola && vigente != null && EstadoPaquete.desdeApi(vigente.estado).name != enviado.estado) {
            dao.guardarAviso(
                enviado.guia,
                "Tu cambio a «${EstadoPaquete.desdeApi(enviado.estado).etiqueta}» no se aplicó: " +
                    "el paquete ya tenía un cambio más reciente " +
                    "(«${EstadoPaquete.desdeApi(vigente.estado).etiqueta}»).",
            )
        }
    }

    private suspend fun rechazar(enviado: PaqueteEntity, mensaje: String) {
        if (dao.resolverSync(enviado.guia, enviado.fechaCambio) == 0) return
        dao.guardarAviso(enviado.guia, "No se pudo sincronizar el cambio: $mensaje")
        // Se restaura el estado real del servidor para no mostrar un cambio que no existe.
        refrescarDetalle(enviado.guia)
    }

    private companion object {
        const val MAX_DESCARGAS_PARALELAS = 4
    }
}
