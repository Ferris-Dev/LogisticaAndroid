package com.trackinglogistic.ui.theme

import androidx.compose.ui.graphics.Color

// Tokens extraídos de cargoexpreso.com/tracking (wp-content/themes/caex-v2/css/custom.css):
//   #0C243E → header, botones COTIZA/PORTAL, campo de guía
//   #D32027 → botón RASTREA (hover #B71F22)
//   #091B32 → variante oscura del azul marino (footer)
//   #1C4587 → azul de enlaces
//   #F1F1F1 → fondos de sección

// Marca
val CaexNavy = Color(0xFF0C243E)
val CaexNavyDark = Color(0xFF091B32)
val CaexNavyDeep = Color(0xFF06121F)
val CaexBlue = Color(0xFF1C4587)
val CaexRed = Color(0xFFD32027)
val CaexRedDark = Color(0xFFB71F22)
val CaexGrayBg = Color(0xFFF1F1F1)
val CaexBorder = Color(0xFFC5D0DC)

// Tonos claros derivados para modo oscuro (mismo matiz, mayor luminosidad para contraste AA)
val CaexNavyTint = Color(0xFFA9C4E6)
val CaexRedTint = Color(0xFFFFB3AE)

// Estados semánticos (contenedor / contenido), modo claro
val PendienteContainerLight = Color(0xFFFFF1CC)
val PendienteOnLight = Color(0xFF7A4B00)
val EnRutaContainerLight = Color(0xFFDCE7F7)
val EnRutaOnLight = CaexBlue
val EntregadoContainerLight = Color(0xFFD7F2DF)
val EntregadoOnLight = Color(0xFF1B6B34)
val NoEntregadoContainerLight = Color(0xFFFBDADB)
val NoEntregadoOnLight = CaexRedDark

// Estados semánticos, modo oscuro
val PendienteContainerDark = Color(0xFF4A3500)
val PendienteOnDark = Color(0xFFFFD98A)
val EnRutaContainerDark = Color(0xFF1B3557)
val EnRutaOnDark = CaexNavyTint
val EntregadoContainerDark = Color(0xFF123D22)
val EntregadoOnDark = Color(0xFF9BE0B0)
val NoEntregadoContainerDark = Color(0xFF5A1416)
val NoEntregadoOnDark = CaexRedTint
