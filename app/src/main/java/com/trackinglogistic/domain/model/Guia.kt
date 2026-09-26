package com.trackinglogistic.domain.model

/**
 * Reglas del código de guía. El usuario puede escribir "10001", "GUA-10001" o "gua 10001";
 * todas se refieren a la misma guía.
 */
object Guia {
    private const val PREFIJO = "GUA-"

    /** El API exige GUA-\d{5}; cualquier otro formato responde 400. */
    const val DIGITOS = 5

    fun numero(texto: String): String = texto.filter(Char::isDigit)

    /**
     * Forma canónica para consultar el API: "10001" → "GUA-10001".
     * Null si no son exactamente 5 dígitos: así no se envía una petición que el API rechazaría.
     */
    fun normalizar(texto: String): String? =
        numero(texto).takeIf { it.length == DIGITOS }?.let { PREFIJO + it }

    /** Filtro local: coincide si los dígitos escritos aparecen en el número de la guía. */
    fun coincide(paquete: Paquete, consulta: String): Boolean {
        if (consulta.isBlank()) return true
        val digitos = numero(consulta)
        if (digitos.isEmpty()) return paquete.guia.contains(consulta.trim(), ignoreCase = true)
        return paquete.numero.contains(digitos)
    }

    fun filtrar(paquetes: List<Paquete>, consulta: String): List<Paquete> =
        if (consulta.isBlank()) paquetes else paquetes.filter { coincide(it, consulta) }
}
