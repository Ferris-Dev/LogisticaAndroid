package com.trackinglogistic.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.trackinglogistic.domain.model.Paquete
import com.trackinglogistic.ui.theme.GuiaTextStyle

@Composable
fun PaqueteCard(
    paquete: Paquete,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    seleccionado: Boolean = false,
) {
    val colors = MaterialTheme.colorScheme
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = if (seleccionado) colors.primaryContainer else colors.surface,
        ),
        border = BorderStroke(
            width = if (seleccionado) 2.dp else 1.dp,
            color = if (seleccionado) colors.primary else colors.outlineVariant,
        ),
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = "#${paquete.numero}",
                        style = GuiaTextStyle,
                        color = colors.onSurface,
                    )
                    Spacer(Modifier.size(2.dp))
                    Text(
                        text = paquete.cliente,
                        style = MaterialTheme.typography.bodyLarge,
                        color = colors.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Spacer(Modifier.width(12.dp))
                EstadoChip(paquete.estado)
            }
            Spacer(Modifier.size(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Icon(
                    Icons.Outlined.Place,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = colors.onSurfaceVariant,
                )
                Text(
                    text = paquete.zona.ifBlank { "Sin zona" },
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (paquete.syncPendiente) {
                Spacer(Modifier.size(8.dp))
                SyncPendienteIndicator()
            }
        }
    }
}

@Composable
fun SyncPendienteIndicator(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(
            Icons.Outlined.CloudUpload,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.tertiary,
        )
        Text(
            text = "Pendiente de sincronizar",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.tertiary,
        )
    }
}
