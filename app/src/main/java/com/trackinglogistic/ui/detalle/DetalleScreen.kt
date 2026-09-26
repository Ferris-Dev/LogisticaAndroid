package com.trackinglogistic.ui.detalle

import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.automirrored.outlined.Notes
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material.icons.outlined.Update
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.trackinglogistic.domain.model.EstadoPaquete
import com.trackinglogistic.domain.model.Paquete
import com.trackinglogistic.domain.usecase.ActualizarEstadoUseCase
import com.trackinglogistic.ui.components.BrandTopBar
import com.trackinglogistic.ui.components.EstadoChip
import com.trackinglogistic.ui.components.MensajeEstado
import com.trackinglogistic.ui.components.SyncPendienteIndicator
import com.trackinglogistic.ui.components.icono
import com.trackinglogistic.ui.theme.GuiaTextStyle
import com.trackinglogistic.ui.theme.TrackingTheme
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * @param onBack null cuando se muestra como panel derecho en tablet (no hay a dónde volver).
 */
@Composable
fun DetalleScreen(
    guia: String,
    viewModel: DetalleViewModel,
    onBack: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(guia) { viewModel.cargar(guia) }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(viewModel) {
        viewModel.eventos.collect { evento ->
            when (evento) {
                is DetalleEvento.Mensaje -> snackbarHostState.showSnackbar(evento.texto)
            }
        }
    }

    DetalleContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onBack = onBack,
        onEstadoSeleccionado = viewModel::onEstadoSeleccionado,
        onObservacionesChange = viewModel::onObservacionesChange,
        onActualizar = viewModel::onActualizar,
        modifier = modifier,
    )
}

@Composable
fun DetalleContent(
    uiState: DetalleUiState,
    snackbarHostState: SnackbarHostState,
    onBack: (() -> Unit)?,
    onEstadoSeleccionado: (EstadoPaquete) -> Unit,
    onObservacionesChange: (String) -> Unit,
    onActualizar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            BrandTopBar(
                titulo = "DETALLE DEL ENVÍO",
                subtitulo = (uiState as? DetalleUiState.Success)?.paquete?.guia,
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                        }
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        Box(
            Modifier
                .padding(padding)
                .fillMaxSize(),
            contentAlignment = Alignment.TopCenter,
        ) {
            when (uiState) {
                DetalleUiState.Loading -> CircularProgressIndicator(Modifier.padding(48.dp))
                is DetalleUiState.Empty -> MensajeEstado(
                    icono = Icons.Outlined.SearchOff,
                    titulo = "Guía no disponible",
                    mensaje = uiState.mensaje,
                )
                is DetalleUiState.Error -> MensajeEstado(
                    icono = Icons.Outlined.ErrorOutline,
                    titulo = "Ocurrió un error",
                    mensaje = uiState.mensaje,
                )
                is DetalleUiState.Success -> DetallePaquete(
                    estado = uiState,
                    onEstadoSeleccionado = onEstadoSeleccionado,
                    onObservacionesChange = onObservacionesChange,
                    onActualizar = onActualizar,
                )
            }
        }
    }
}

@Composable
private fun DetallePaquete(
    estado: DetalleUiState.Success,
    onEstadoSeleccionado: (EstadoPaquete) -> Unit,
    onObservacionesChange: (String) -> Unit,
    onActualizar: () -> Unit,
) {
    val paquete = estado.paquete
    Column(
        modifier = Modifier
            .widthIn(max = 640.dp)
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        paquete.avisoSync?.let { AvisoSync(it) }
        EncabezadoGuia(paquete)
        DatosDestinatario(paquete, cargandoDetalle = estado.cargandoDetalle)
        CambioEstado(estado, onEstadoSeleccionado, onObservacionesChange, onActualizar)
    }
}

@Composable
private fun EncabezadoGuia(paquete: Paquete) {
    val brand = TrackingTheme.brand
    Surface(
        color = brand.header,
        contentColor = brand.onHeader,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("NRO. DE GUÍA", style = MaterialTheme.typography.labelMedium, color = brand.onHeader.copy(alpha = 0.7f))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "#${paquete.numero}",
                    style = GuiaTextStyle.copy(fontSize = GuiaTextStyle.fontSize * 1.5f),
                    modifier = Modifier.weight(1f),
                )
                EstadoChip(paquete.estado)
            }
            Text(paquete.guia, style = MaterialTheme.typography.bodyMedium, color = brand.onHeader.copy(alpha = 0.8f))
        }
    }
}

@Composable
private fun DatosDestinatario(paquete: Paquete, cargandoDetalle: Boolean) {
    val context = LocalContext.current
    // La lista solo trae el resumen: teléfono y dirección llegan con el detalle del API.
    val pendiente = when {
        paquete.detalleCargado -> null
        cargandoDetalle -> "Cargando…"
        else -> "Disponible al recuperar la conexión"
    }
    Card(
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(Modifier.padding(vertical = 8.dp)) {
            FilaDato(Icons.Outlined.Person, "Cliente", paquete.cliente)
            HorizontalDivider(Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant)
            FilaDato(
                icono = Icons.Outlined.Phone,
                etiqueta = "Teléfono",
                valor = pendiente ?: paquete.telefono.ifBlank { "Sin teléfono" },
                accionIcono = if (paquete.telefono.isNotBlank()) Icons.Outlined.Call else null,
                onClick = if (paquete.telefono.isBlank()) null else {
                    {
                        // ACTION_DIAL abre el marcador con el número; no requiere permiso CALL_PHONE.
                        val intent = Intent(Intent.ACTION_DIAL, "tel:${paquete.telefono}".toUri())
                        try {
                            context.startActivity(intent)
                        } catch (_: ActivityNotFoundException) {
                            // Dispositivo sin app de teléfono (algunas tablets): no hay nada que abrir.
                        }
                    }
                },
            )
            HorizontalDivider(Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant)
            FilaDato(
                Icons.Outlined.Home,
                "Dirección de entrega",
                pendiente ?: paquete.direccion.ifBlank { "Sin dirección" },
            )
            HorizontalDivider(Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant)
            FilaDato(Icons.Outlined.Map, "Zona", paquete.zona.ifBlank { "Sin zona" })
            formatearFechaLocal(paquete.ultimaActualizacion)?.let { fecha ->
                HorizontalDivider(Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant)
                FilaDato(Icons.Outlined.Update, "Última actualización", fecha)
            }
            if (!paquete.observaciones.isNullOrBlank()) {
                HorizontalDivider(Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant)
                FilaDato(Icons.AutoMirrored.Outlined.Notes, "Observaciones", paquete.observaciones)
            }
        }
    }
}

@Composable
private fun FilaDato(
    icono: ImageVector,
    etiqueta: String,
    valor: String,
    accionIcono: ImageVector? = null,
    onClick: (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(role = Role.Button, onClick = onClick) else Modifier)
            .heightIn(min = 56.dp)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icono, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Column(
            Modifier
                .weight(1f)
                .padding(start = 16.dp),
        ) {
            Text(etiqueta, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                valor,
                style = MaterialTheme.typography.bodyLarge,
                color = if (onClick != null) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface,
            )
        }
        if (accionIcono != null) {
            Icon(accionIcono, contentDescription = "Llamar", tint = MaterialTheme.colorScheme.secondary)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CambioEstado(
    estado: DetalleUiState.Success,
    onEstadoSeleccionado: (EstadoPaquete) -> Unit,
    onObservacionesChange: (String) -> Unit,
    onActualizar: () -> Unit,
) {
    val brand = TrackingTheme.brand
    var expandido by rememberSaveable { mutableStateOf(false) }

    Card(
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("ACTUALIZAR ESTADO", style = MaterialTheme.typography.titleMedium)

            ExposedDropdownMenuBox(expanded = expandido, onExpandedChange = { expandido = it }) {
                OutlinedTextField(
                    value = estado.estadoSeleccionado.etiqueta,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Nuevo estado") },
                    leadingIcon = {
                        Icon(
                            estado.estadoSeleccionado.icono,
                            contentDescription = null,
                            tint = brand.de(estado.estadoSeleccionado).contenido,
                        )
                    },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandido) },
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth(),
                )
                ExposedDropdownMenu(expanded = expandido, onDismissRequest = { expandido = false }) {
                    EstadoPaquete.entries.forEach { opcion ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    if (opcion == estado.paquete.estado) "${opcion.etiqueta} (actual)" else opcion.etiqueta,
                                )
                            },
                            leadingIcon = {
                                Icon(opcion.icono, contentDescription = null, tint = brand.de(opcion).contenido)
                            },
                            onClick = {
                                onEstadoSeleccionado(opcion)
                                expandido = false
                            },
                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                        )
                    }
                }
            }

            val esNoEntregado = estado.estadoSeleccionado == EstadoPaquete.NO_ENTREGADO
            OutlinedTextField(
                value = estado.observaciones,
                onValueChange = onObservacionesChange,
                label = { Text(if (esNoEntregado) "Motivo de no entrega (opcional)" else "Observaciones (opcional)") },
                placeholder = { if (esNoEntregado) Text("Ej.: Local cerrado") },
                supportingText = {
                    Text("${estado.observaciones.length}/${ActualizarEstadoUseCase.MAX_OBSERVACIONES}")
                },
                shape = MaterialTheme.shapes.small,
                minLines = 2,
                maxLines = 4,
                modifier = Modifier.fillMaxWidth(),
            )

            if (estado.paquete.syncPendiente) {
                SyncPendienteIndicator()
                Text(
                    "El último cambio está guardado en el teléfono y se enviará automáticamente al recuperar la conexión.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Button(
                onClick = onActualizar,
                enabled = estado.puedeActualizar,
                shape = MaterialTheme.shapes.small,
                colors = ButtonDefaults.buttonColors(
                    containerColor = brand.accion,
                    contentColor = brand.onAccion,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp),
            ) {
                if (estado.actualizando) {
                    CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp, color = brand.onAccion)
                } else {
                    Text("ACTUALIZAR ESTADO")
                }
            }
            if (!estado.puedeActualizar && !estado.actualizando) {
                Text(
                    "Elige un estado distinto al actual para habilitar el botón.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

/** El cambio del piloto no se aplicó en el servidor (superado por uno más reciente o rechazado). */
@Composable
private fun AvisoSync(mensaje: String) {
    Surface(
        color = MaterialTheme.colorScheme.tertiaryContainer,
        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.WarningAmber, contentDescription = null)
            Text(mensaje, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(start = 12.dp))
        }
    }
}

// El servidor envía UTC; se muestra en la hora de Guatemala (UTC-6), donde operan los pilotos.
private val ZONA_PILOTOS: ZoneId = ZoneId.of("America/Guatemala")
private val FORMATO_FECHA: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")

private fun formatearFechaLocal(isoUtc: String?): String? =
    isoUtc?.let { runCatching { FORMATO_FECHA.format(Instant.parse(it).atZone(ZONA_PILOTOS)) }.getOrNull() }
