package com.trackinglogistic.fakes

import com.trackinglogistic.domain.model.EstadoPaquete
import com.trackinglogistic.domain.model.Paquete

fun paquete(
    guia: String,
    estado: EstadoPaquete = EstadoPaquete.PENDIENTE,
    cliente: String = "Cliente $guia",
    zona: String = "Zona 10",
) = Paquete(
    guia = guia,
    cliente = cliente,
    telefono = "55551234",
    direccion = "6a Avenida 1-23, Zona 10, Guatemala",
    zona = zona,
    estado = estado,
)
