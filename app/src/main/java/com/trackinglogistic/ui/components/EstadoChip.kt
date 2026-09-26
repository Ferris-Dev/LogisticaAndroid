package com.trackinglogistic.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.HighlightOff
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.trackinglogistic.domain.model.EstadoPaquete
import com.trackinglogistic.ui.theme.TrackingTheme

val EstadoPaquete.icono: ImageVector
    get() = when (this) {
        EstadoPaquete.PENDIENTE -> Icons.Outlined.Schedule
        EstadoPaquete.EN_RUTA -> Icons.Outlined.LocalShipping
        EstadoPaquete.ENTREGADO -> Icons.Outlined.CheckCircle
        EstadoPaquete.NO_ENTREGADO -> Icons.Outlined.HighlightOff
    }

/** Chip de estado: color semántico + ícono + texto (nunca solo color, por accesibilidad). */
@Composable
fun EstadoChip(estado: EstadoPaquete, modifier: Modifier = Modifier) {
    val colores = TrackingTheme.brand.de(estado)
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = colores.contenedor,
        contentColor = colores.contenido,
    ) {
        Row(
            modifier = Modifier.padding(start = 8.dp, end = 10.dp, top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(estado.icono, contentDescription = null, modifier = Modifier.size(16.dp))
            Text(estado.etiqueta.uppercase(), style = MaterialTheme.typography.labelMedium)
        }
    }
}
