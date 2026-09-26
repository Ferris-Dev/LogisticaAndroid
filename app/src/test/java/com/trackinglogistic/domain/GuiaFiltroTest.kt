package com.trackinglogistic.domain

import com.trackinglogistic.domain.model.Guia
import com.trackinglogistic.fakes.paquete
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GuiaFiltroTest {

    private val paquetes = listOf(
        paquete("GUA-10001"),
        paquete("GUA-10002"),
        paquete("GUA-20001"),
    )

    private fun filtrar(consulta: String) = Guia.filtrar(paquetes, consulta).map { it.guia }

    @Test
    fun `consulta vacia devuelve todos`() {
        assertEquals(3, filtrar("").size)
        assertEquals(3, filtrar("   ").size)
    }

    @Test
    fun `acepta solo el numero`() {
        assertEquals(listOf("GUA-10001"), filtrar("10001"))
    }

    @Test
    fun `acepta el codigo completo con prefijo`() {
        assertEquals(listOf("GUA-10001"), filtrar("GUA-10001"))
    }

    @Test
    fun `ignora mayusculas, espacios y el numeral`() {
        assertEquals(listOf("GUA-10001"), filtrar(" gua-10001 "))
        assertEquals(listOf("GUA-10001"), filtrar("#10001"))
    }

    @Test
    fun `coincidencia parcial filtra mientras se escribe`() {
        assertEquals(listOf("GUA-10001", "GUA-10002"), filtrar("1000"))
    }

    @Test
    fun `sin coincidencias devuelve lista vacia`() {
        assertEquals(emptyList<String>(), filtrar("99999"))
    }

    @Test
    fun `normalizar produce la guia canonica para el API`() {
        assertEquals("GUA-10001", Guia.normalizar("10001"))
        assertEquals("GUA-10001", Guia.normalizar("gua-10001"))
        assertNull(Guia.normalizar("GUA-"))
    }

    @Test
    fun `normalizar rechaza lo que no sea GUA seguido de 5 digitos`() {
        // El API responde 400 si la guía no cumple GUA-\d{5}: no se debe ni enviar.
        assertNull(Guia.normalizar("123"))
        assertNull(Guia.normalizar("1000"))
        assertNull(Guia.normalizar("100011"))
    }
}
