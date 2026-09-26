package com.trackinglogistic.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.TouchApp
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.toRoute
import com.trackinglogistic.ui.components.BrandTopBar
import com.trackinglogistic.ui.components.MensajeEstado
import com.trackinglogistic.ui.detalle.DetalleScreen
import com.trackinglogistic.ui.detalle.DetalleViewModel
import com.trackinglogistic.ui.paquetes.PaquetesScreen
import com.trackinglogistic.ui.paquetes.PaquetesViewModel
import kotlinx.serialization.Serializable

@Serializable
data object ListaRoute

@Serializable
data class DetalleRoute(val guia: String)

/**
 * Navegación adaptativa según WindowSizeClass:
 * - Compact (teléfono vertical): lista → detalle con Navigation Compose.
 * - Medium/Expanded (tablet, teléfono horizontal, plegables): lista y detalle lado a lado.
 */
@Composable
fun TrackingApp(widthSizeClass: WindowWidthSizeClass) {
    // Un solo PaquetesViewModel a nivel de Activity: sobrevive al cambio entre
    // layouts (rotación) sin volver a refrescar ni perder la búsqueda.
    val paquetesViewModel: PaquetesViewModel = hiltViewModel()
    var guiaSeleccionada by rememberSaveable { mutableStateOf<String?>(null) }

    if (widthSizeClass == WindowWidthSizeClass.Compact) {
        NavegacionCompacta(
            paquetesViewModel = paquetesViewModel,
            guiaInicial = guiaSeleccionada,
            onSeleccion = { guiaSeleccionada = it },
        )
    } else {
        ListaDetalleLadoALado(
            paquetesViewModel = paquetesViewModel,
            guiaSeleccionada = guiaSeleccionada,
            onSeleccion = { guiaSeleccionada = it },
        )
    }
}

@Composable
private fun NavegacionCompacta(
    paquetesViewModel: PaquetesViewModel,
    guiaInicial: String?,
    onSeleccion: (String?) -> Unit,
) {
    val navController = rememberNavController()

    // Si se venía del modo tablet con un paquete abierto, se mantiene abierto al pasar a compacto.
    LaunchedEffect(Unit) {
        if (guiaInicial != null) navController.navigate(DetalleRoute(guiaInicial))
    }
    // La selección se deriva del back stack, así también se limpia con el gesto "atrás" del sistema.
    LaunchedEffect(navController) {
        navController.currentBackStackEntryFlow.collect { entry ->
            if (entry.destination.hasRoute<DetalleRoute>()) {
                onSeleccion(entry.toRoute<DetalleRoute>().guia)
            } else if (entry.destination.hasRoute<ListaRoute>() && navController.previousBackStackEntry == null) {
                onSeleccion(null)
            }
        }
    }

    NavHost(navController = navController, startDestination = ListaRoute) {
        composable<ListaRoute> {
            PaquetesScreen(
                viewModel = paquetesViewModel,
                onAbrirDetalle = { guia ->
                    navController.navigate(DetalleRoute(guia)) { launchSingleTop = true }
                },
            )
        }
        composable<DetalleRoute> { entry ->
            val route = entry.toRoute<DetalleRoute>()
            val viewModel: DetalleViewModel = hiltViewModel()
            DetalleScreen(
                guia = route.guia,
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
            )
        }
    }
}

@Composable
private fun ListaDetalleLadoALado(
    paquetesViewModel: PaquetesViewModel,
    guiaSeleccionada: String?,
    onSeleccion: (String?) -> Unit,
) {
    Row(Modifier.fillMaxSize()) {
        PaquetesScreen(
            viewModel = paquetesViewModel,
            onAbrirDetalle = onSeleccion,
            guiaSeleccionada = guiaSeleccionada,
            modifier = Modifier
                .weight(0.45f)
                .fillMaxHeight(),
        )
        VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Box(
            Modifier
                .weight(0.55f)
                .fillMaxHeight(),
        ) {
            if (guiaSeleccionada == null) {
                // Mismo header que el detalle, para que la barra azul sea continua entre ambos paneles.
                Scaffold(
                    topBar = { BrandTopBar(titulo = "DETALLE DEL ENVÍO", subtitulo = null) },
                    containerColor = MaterialTheme.colorScheme.background,
                ) { padding ->
                    Box(
                        Modifier
                            .padding(padding)
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        MensajeEstado(
                            icono = Icons.Outlined.TouchApp,
                            titulo = "Selecciona un paquete",
                            mensaje = "Elige una guía de la lista para ver su detalle y actualizar su estado.",
                        )
                    }
                }
            } else {
                val detalleViewModel: DetalleViewModel = hiltViewModel()
                DetalleScreen(
                    guia = guiaSeleccionada,
                    viewModel = detalleViewModel,
                    onBack = null,
                )
            }
        }
    }
}
