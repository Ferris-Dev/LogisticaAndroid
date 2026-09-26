package com.trackinglogistic.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.trackinglogistic.ui.theme.TrackingTheme

/**
 * Buscador de guía al estilo del campo "INGRESA # DE GUÍA" + botón rojo "RASTREA"
 * de cargoexpreso.com/tracking: input grande, esquinas de 10dp, sobre banda azul marino.
 */
@Composable
fun GuiaSearchBar(
    consulta: String,
    onConsultaChange: (String) -> Unit,
    onBuscar: () -> Unit,
    buscando: Boolean,
    modifier: Modifier = Modifier,
    botonCompacto: Boolean = false,
) {
    val brand = TrackingTheme.brand
    val focusManager = LocalFocusManager.current
    val buscar = {
        focusManager.clearFocus()
        onBuscar()
    }
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        TextField(
            value = consulta,
            onValueChange = onConsultaChange,
            modifier = Modifier
                .weight(1f)
                .heightIn(min = 56.dp),
            placeholder = {
                Text(
                    "# DE GUÍA",
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            trailingIcon = {
                if (consulta.isNotEmpty()) {
                    IconButton(onClick = { onConsultaChange("") }) {
                        Icon(Icons.Filled.Close, contentDescription = "Limpiar búsqueda")
                    }
                }
            },
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyLarge,
            shape = MaterialTheme.shapes.small,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Characters,
                imeAction = ImeAction.Search,
            ),
            keyboardActions = KeyboardActions(onSearch = { buscar() }),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedTextColor = Color(0xFF14202E),
                unfocusedTextColor = Color(0xFF14202E),
                focusedPlaceholderColor = Color(0xFF6B7785),
                unfocusedPlaceholderColor = Color(0xFF6B7785),
                focusedLeadingIconColor = Color(0xFF0C243E),
                unfocusedLeadingIconColor = Color(0xFF6B7785),
                focusedTrailingIconColor = Color(0xFF6B7785),
                unfocusedTrailingIconColor = Color(0xFF6B7785),
                cursorColor = brand.accion,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
            ),
        )
        Button(
            onClick = buscar,
            enabled = consulta.isNotBlank() && !buscando,
            shape = MaterialTheme.shapes.small,
            colors = ButtonDefaults.buttonColors(
                containerColor = brand.accion,
                contentColor = brand.onAccion,
                disabledContainerColor = brand.accion.copy(alpha = 0.55f),
                disabledContentColor = brand.onAccion.copy(alpha = 0.8f),
            ),
            contentPadding = if (botonCompacto) PaddingValues(horizontal = 16.dp) else PaddingValues(horizontal = 24.dp),
            modifier = Modifier.heightIn(min = 56.dp),
        ) {
            if (buscando) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = brand.onAccion,
                )
            } else if (botonCompacto) {
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Rastrear")
            } else {
                Text("RASTREA")
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .size(18.dp),
                )
            }
        }
    }
}
