package com.trackinglogistic.data

import com.trackinglogistic.data.remote.dto.ProblemDetailsDto
import com.trackinglogistic.data.remote.errorEnCampo
import com.trackinglogistic.data.remote.mensajeDeError
import com.trackinglogistic.data.remote.problemDetails
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response

/** Lectura de errores RFC 7807 tal como los envía el backend. */
class ProblemDetailsTest {

    private val json = Json { ignoreUnknownKeys = true }

    private fun error(codigo: Int, cuerpo: String): Response<Unit> =
        Response.error(codigo, cuerpo.toResponseBody("application/problem+json".toMediaType()))

    @Test
    fun `404 muestra el detail del servidor e ignora type y traceId`() {
        val respuesta = error(
            404,
            """{"type":"https://tools.ietf.org/html/rfc9110#section-15.5.5","title":"Guía no encontrada",
               "status":404,"detail":"La guía GUA-99999 no existe","traceId":"00-abc-01"}""",
        )

        assertEquals("La guía GUA-99999 no existe", mensajeDeError(404, respuesta.problemDetails(json)))
    }

    @Test
    fun `400 con errors por campo detecta el campo fechaCambio`() {
        val problema = error(
            400,
            """{"title":"Solicitud inválida","status":400,"detail":"Uno o más campos no son válidos",
               "errors":{"fechaCambio":["No puede estar en el futuro"]}}""",
        ).problemDetails(json)!!

        assertTrue(problema.errorEnCampo("fechaCambio"))
        assertEquals("Uno o más campos no son válidos", mensajeDeError(400, problema))
    }

    @Test
    fun `500 muestra un mensaje generico, nunca el cuerpo crudo`() {
        val problema = ProblemDetailsDto(detail = "NullReferenceException at Foo.Bar()")

        assertEquals("Error del servidor, intenta de nuevo", mensajeDeError(500, problema))
    }

    @Test
    fun `cuerpo que no es JSON no rompe y usa mensaje de respaldo`() {
        val problema = error(400, "<html>Bad Request</html>").problemDetails(json)

        assertNull(problema)
        assertEquals("Error inesperado (400)", mensajeDeError(400, problema))
    }
}
