package com.trackinglogistic.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.trackinglogistic.R

// Mismas familias que cargoexpreso.com (Google Fonts, licencia OFL), empaquetadas en res/font
// para no depender de red ni de Play Services.
// Oswald → titulares y guías (condensada, en mayúsculas en el sitio); Archivo → texto.
val Oswald = FontFamily(
    Font(R.font.oswald_500, FontWeight.Medium),
    Font(R.font.oswald_600, FontWeight.SemiBold),
)

val Archivo = FontFamily(
    Font(R.font.archivo_400, FontWeight.Normal),
    Font(R.font.archivo_500, FontWeight.Medium),
    Font(R.font.archivo_700, FontWeight.Bold),
)

private val base = Typography()

val Typography = Typography(
    displaySmall = base.displaySmall.copy(fontFamily = Oswald, fontWeight = FontWeight.SemiBold),
    headlineMedium = base.headlineMedium.copy(fontFamily = Oswald, fontWeight = FontWeight.SemiBold),
    headlineSmall = base.headlineSmall.copy(fontFamily = Oswald, fontWeight = FontWeight.SemiBold),
    titleLarge = base.titleLarge.copy(fontFamily = Oswald, fontWeight = FontWeight.Medium, letterSpacing = 0.5.sp),
    titleMedium = base.titleMedium.copy(fontFamily = Oswald, fontWeight = FontWeight.SemiBold, letterSpacing = 0.3.sp),
    titleSmall = base.titleSmall.copy(fontFamily = Archivo, fontWeight = FontWeight.Bold),
    bodyLarge = base.bodyLarge.copy(fontFamily = Archivo),
    bodyMedium = base.bodyMedium.copy(fontFamily = Archivo),
    bodySmall = base.bodySmall.copy(fontFamily = Archivo),
    labelLarge = base.labelLarge.copy(fontFamily = Archivo, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp),
    labelMedium = base.labelMedium.copy(fontFamily = Archivo, fontWeight = FontWeight.Bold),
    labelSmall = base.labelSmall.copy(fontFamily = Archivo, fontWeight = FontWeight.Medium),
)

/** Estilo del número de guía en tarjetas y detalle. */
val GuiaTextStyle = TextStyle(
    fontFamily = Oswald,
    fontWeight = FontWeight.SemiBold,
    fontSize = 20.sp,
    letterSpacing = 0.5.sp,
)
