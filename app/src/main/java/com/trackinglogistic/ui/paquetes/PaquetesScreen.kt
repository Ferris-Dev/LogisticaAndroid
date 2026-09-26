package com.trackinglogistic.ui.paquetes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material.icons.outlined.PersonOff
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.trackinglogistic.domain.model.Paquete
import com.trackinglogistic.ui.components.BrandTopBar
import com.trackinglogistic.ui.components.GuiaSearchBar
import com.trackinglogistic.ui.components.MensajeEstado
import com.trackinglogistic.ui.components.PaqueteCard
import com.trackinglogistic.ui.theme.TrackingTheme

@Composable
fun PaquetesScreen(
    viewModel: PaquetesViewModel,
    onAbrirDetalle: (String) -> Unit,
    modifier: Modifier = Modifier,
    guiaSeleccionada: String? = null,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val consulta by viewModel.consulta.collectAsStateWithLifecycle()
    val refrescando by viewModel.refrescando.collectAsStateWithLifecycle()
    val busqueda by viewModel.busquedaRemota.collectAsStateWithLifecycle()
    val pilotoId by viewModel.pilotoId.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val abrirDetalle by rememberUpdatedState(onAbrirDetalle)
    LaunchedEffect(viewModel) {
        viewModel.eventos.collect { evento ->
            when (evento) {
                is PaquetesEvento.AbrirDetalle -> abrirDetalle(evento.guia)
                is PaquetesEvento.Mensaje -> snackbarHostState.showSnackbar(evento.texto)
            }
        }
    }

    PaquetesContent(
        uiState = uiState,
        consulta = consulta,
        refrescando = refrescando,
        busqueda = busqueda,
        pilotoId = pilotoId,
        guiaSeleccionada = guiaSeleccionada,
        snackbarHostState = snackbarHostState,
        onConsultaChange = viewModel::onConsultaChange,
        onBuscar = viewModel::onBuscar,
        onRefresh = viewModel::onRefresh,
        onCambiarPiloto = viewModel::onCambiarPiloto,
        onAbrirDetalle = onAbrirDetalle,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaquetesContent(
    uiState: PaquetesUiState,
    consulta: String,
    refrescando: Boolean,
    busqueda: BusquedaRemotaState,
    pilotoId: String?,
    guiaSeleccionada: String?,
    snackbarHostState: SnackbarHostState,
    onConsultaChange: (String) -> Unit,
    onBuscar: () -> Unit,
    onRefresh: () -> Unit,
    onCambiarPiloto: () -> Unit,
    onAbrirDetalle: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            BrandTopBar(
                subtitulo = pilotoId?.let { "Piloto $it" },
                actions = {
                    IconButton(onClick = onCambiarPiloto) {
                        Icon(Icons.AutoMirrored.Outlined.Logout, contentDescription = "Cambiar piloto")
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        val direccion = LocalLayoutDirection.current
        // Los insets laterales (notch en horizontal) se aplican al contenido, no al fondo,
        // para que la banda azul llegue de borde a borde.
        val inicio = padding.calculateStartPadding(direccion)
        val fin = padding.calculateEndPadding(direccion)
        Column(
            Modifier
                .padding(top = padding.calculateTopPadding())
                .fillMaxSize(),
        ) {
            // Banda del buscador: continúa el color del header, como el hero del sitio.
            Surface(color = TrackingTheme.brand.header, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(start = 16.dp + inicio, end = 16.dp + fin, top = 4.dp, bottom = 16.dp)) {
                    Text(
                        "RASTREA TU ENVÍO",
                        style = MaterialTheme.typography.titleMedium,
                        color = TrackingTheme.brand.onHeader,
                        modifier = Modifier.padding(bottom = 8.dp),
                    )
                    BoxWithConstraints(Modifier.fillMaxWidth()) {
                        GuiaSearchBar(
                            consulta = consulta,
                            onConsultaChange = onConsultaChange,
                            onBuscar = onBuscar,
                            buscando = busqueda == BusquedaRemotaState.Buscando,
                            // En teléfonos angostos el botón queda solo con ícono para no apretar el campo.
                            botonCompacto = maxWidth < 340.dp,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }

            PullToRefreshBox(
                isRefreshing = refrescando,
                onRefresh = onRefresh,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = inicio, end = fin, bottom = padding.calculateBottomPadding()),
            ) {
                when (uiState) {
                    PaquetesUiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                    // Los estados vacíos también van en LazyColumn para que el pull-to-refresh funcione.
                    is PaquetesUiState.Empty -> ListaDesplazable {
                        item {
                            MensajeEstado(
                                icono = if (uiState.sinConexion) Icons.Outlined.CloudOff else Icons.Outlined.Inbox,
                                titulo = uiState.titulo,
                                mensaje = uiState.mensaje,
                                accion = "Reintentar",
                                onAccion = onRefresh,
                            )
                        }
                    }
                    is PaquetesUiState.Error -> ListaDesplazable {
                        item {
                            if (uiState.pilotoInvalido) {
                                // 404: error de configuración, reintentar no sirve; hay que cambiar el código.
                                MensajeEstado(
                                    icono = Icons.Outlined.PersonOff,
                                    titulo = "Piloto no encontrado",
                                    mensaje = "${uiState.mensaje}. Verifica tu código de piloto.",
                                    accion = "Cambiar piloto",
                                    onAccion = onCambiarPiloto,
                                )
                            } else {
                                MensajeEstado(
                                    icono = Icons.Outlined.ErrorOutline,
                                    titulo = "No se pudieron cargar los paquetes",
                                    mensaje = uiState.mensaje,
                                    accion = "Reintentar",
                                    onAccion = onRefresh,
                                )
                            }
                        }
                    }
                    is PaquetesUiState.Success -> ListaPaquetes(
                        paquetes = uiState.paquetes,
                        filtrando = uiState.filtrando,
                        consulta = consulta,
                        busqueda = busqueda,
                        guiaSeleccionada = guiaSeleccionada,
                        onBuscar = onBuscar,
                        onAbrirDetalle = onAbrirDetalle,
                    )
                }
            }
        }
    }
}

@Composable
private fun ListaDesplazable(content: LazyListScope.() -> Unit) {
    LazyColumn(Modifier.fillMaxSize(), content = content)
}

@Composable
private fun ListaPaquetes(
    paquetes: List<Paquete>,
    filtrando: Boolean,
    consulta: String,
    busqueda: BusquedaRemotaState,
    guiaSeleccionada: String?,
    onBuscar: () -> Unit,
    onAbrirDetalle: (String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item(key = "encabezado") {
            Text(
                text = if (filtrando) "RESULTADOS (${paquetes.size})" else "MIS PAQUETES (${paquetes.size})",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }
        if (paquetes.isEmpty() && filtrando) {
            item(key = "sin-coincidencias") {
                SinCoincidencias(consulta = consulta, busqueda = busqueda, onBuscar = onBuscar)
            }
        }
        items(paquetes, key = { it.guia }) { paquete ->
            PaqueteCard(
                paquete = paquete,
                onClick = { onAbrirDetalle(paquete.guia) },
                seleccionado = paquete.guia == guiaSeleccionada,
            )
        }
    }
}

/** Sin coincidencias locales: invita a consultar el servidor y muestra su resultado. */
@Composable
private fun SinCoincidencias(consulta: String, busqueda: BusquedaRemotaState, onBuscar: () -> Unit) {
    when (busqueda) {
        BusquedaRemotaState.Buscando -> Row(
            Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
            Text("  Consultando la guía en el servidor…", style = MaterialTheme.typography.bodyMedium)
        }
        is BusquedaRemotaState.NoExiste -> MensajeEstado(
            icono = Icons.Outlined.SearchOff,
            titulo = "La guía no existe",
            mensaje = busqueda.mensaje,
        )
        is BusquedaRemotaState.FormatoInvalido -> MensajeEstado(
            icono = Icons.Outlined.ErrorOutline,
            titulo = "Formato de guía inválido",
            mensaje = busqueda.mensaje,
        )
        is BusquedaRemotaState.Fallo -> MensajeEstado(
            icono = Icons.Outlined.CloudOff,
            titulo = "No se pudo consultar",
            mensaje = busqueda.mensaje,
            accion = "Reintentar",
            onAccion = onBuscar,
        )
        BusquedaRemotaState.Inactiva -> MensajeEstado(
            icono = Icons.Outlined.SearchOff,
            titulo = "No está en tu lista",
            mensaje = "Ninguno de tus paquetes coincide con «${consulta.trim()}». " +
                "Presiona RASTREA para buscarla en el servidor.",
            accion = "Buscar en el servidor",
            onAccion = onBuscar,
        )
    }
}
