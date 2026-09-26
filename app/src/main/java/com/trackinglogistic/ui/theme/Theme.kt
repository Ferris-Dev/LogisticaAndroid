package com.trackinglogistic.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.trackinglogistic.domain.model.EstadoPaquete

private val LightColors = lightColorScheme(
    primary = CaexNavy,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDCE7F7),
    onPrimaryContainer = CaexNavy,
    secondary = CaexBlue,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE3EAF3),
    onSecondaryContainer = CaexNavy,
    tertiary = CaexRed,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFBDADB),
    onTertiaryContainer = CaexRedDark,
    error = CaexRedDark,
    background = CaexGrayBg,
    onBackground = Color(0xFF14202E),
    surface = Color.White,
    onSurface = Color(0xFF14202E),
    surfaceVariant = Color(0xFFE8EDF3),
    onSurfaceVariant = Color(0xFF4A5868),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFF7F8FA),
    surfaceContainer = Color(0xFFF1F3F6),
    surfaceContainerHigh = Color(0xFFEBEEF2),
    surfaceContainerHighest = Color(0xFFE4E8ED),
    outline = CaexBorder,
    outlineVariant = Color(0xFFDDE3EA),
)

private val DarkColors = darkColorScheme(
    primary = CaexNavyTint,
    onPrimary = CaexNavyDeep,
    primaryContainer = CaexNavy,
    onPrimaryContainer = Color(0xFFDCE7F7),
    secondary = Color(0xFF9DB7DB),
    onSecondary = CaexNavyDeep,
    secondaryContainer = Color(0xFF1B3557),
    onSecondaryContainer = Color(0xFFDCE7F7),
    tertiary = Color(0xFFFF6B6F),
    onTertiary = Color(0xFF3B0003),
    tertiaryContainer = CaexRedDark,
    onTertiaryContainer = Color.White,
    error = CaexRedTint,
    background = CaexNavyDeep,
    onBackground = Color(0xFFE3E8EF),
    surface = CaexNavyDark,
    onSurface = Color(0xFFE3E8EF),
    surfaceVariant = Color(0xFF1A2B40),
    onSurfaceVariant = Color(0xFFB4C0CE),
    surfaceContainerLowest = CaexNavyDeep,
    surfaceContainerLow = Color(0xFF0B1F35),
    surfaceContainer = Color(0xFF10263F),
    surfaceContainerHigh = Color(0xFF152D48),
    surfaceContainerHighest = Color(0xFF1B3452),
    outline = Color(0xFF4A5E75),
    outlineVariant = Color(0xFF2A3D54),
)

// Radios del sitio: botones/campo de guía 10px, tarjetas 15px.
private val CaexShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(15.dp),
    large = RoundedCornerShape(20.dp),
)

@Immutable
data class ColoresEstado(val contenedor: Color, val contenido: Color)

@Immutable
data class BrandColors(
    /** TopAppBar y banda del buscador: siempre azul marino, como el header del sitio. */
    val header: Color,
    val onHeader: Color,
    /** Botón RASTREA: rojo de la marca en ambos modos. */
    val accion: Color,
    val onAccion: Color,
    val pendiente: ColoresEstado,
    val enRuta: ColoresEstado,
    val entregado: ColoresEstado,
    val noEntregado: ColoresEstado,
) {
    fun de(estado: EstadoPaquete): ColoresEstado = when (estado) {
        EstadoPaquete.PENDIENTE -> pendiente
        EstadoPaquete.EN_RUTA -> enRuta
        EstadoPaquete.ENTREGADO -> entregado
        EstadoPaquete.NO_ENTREGADO -> noEntregado
    }
}

private val LightBrand = BrandColors(
    header = CaexNavy,
    onHeader = Color.White,
    accion = CaexRed,
    onAccion = Color.White,
    pendiente = ColoresEstado(PendienteContainerLight, PendienteOnLight),
    enRuta = ColoresEstado(EnRutaContainerLight, EnRutaOnLight),
    entregado = ColoresEstado(EntregadoContainerLight, EntregadoOnLight),
    noEntregado = ColoresEstado(NoEntregadoContainerLight, NoEntregadoOnLight),
)

private val DarkBrand = BrandColors(
    header = CaexNavyDark,
    onHeader = Color.White,
    accion = CaexRed,
    onAccion = Color.White,
    pendiente = ColoresEstado(PendienteContainerDark, PendienteOnDark),
    enRuta = ColoresEstado(EnRutaContainerDark, EnRutaOnDark),
    entregado = ColoresEstado(EntregadoContainerDark, EntregadoOnDark),
    noEntregado = ColoresEstado(NoEntregadoContainerDark, NoEntregadoOnDark),
)

val LocalBrandColors = staticCompositionLocalOf { LightBrand }

object TrackingTheme {
    val brand: BrandColors
        @Composable get() = LocalBrandColors.current
}

/** Tema propio de Cargo Expreso. Dynamic color desactivado a propósito: la marca manda. */
@Composable
fun TrackingLogisticTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalBrandColors provides if (darkTheme) DarkBrand else LightBrand) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColors else LightColors,
            typography = Typography,
            shapes = CaexShapes,
            content = content,
        )
    }
}
