package com.trackinglogistic.domain.model

/** Código de piloto con formato PIL-000. Acepta "1", "001", "pil-1" o "PIL-001". */
object CodigoPiloto {
    private val FORMATO = Regex("""^PIL-?(\d{1,3})$""")

    fun normalizar(texto: String): String? {
        val limpio = texto.trim().uppercase()
        val digitos = if (limpio.all(Char::isDigit)) limpio else FORMATO.find(limpio)?.groupValues?.get(1)
        if (digitos.isNullOrEmpty() || digitos.length > 3) return null
        return "PIL-" + digitos.padStart(3, '0')
    }

    /** El API identifica al piloto por su id numérico: "PIL-001" → 1. */
    fun idNumerico(codigo: String): Int? = FORMATO.find(codigo.trim().uppercase())?.groupValues?.get(1)?.toIntOrNull()
}
