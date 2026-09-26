package com.trackinglogistic.ui.piloto

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.trackinglogistic.ui.theme.TrackingTheme

@Composable
fun PilotoScreen(viewModel: PilotoViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    PilotoContent(
        uiState = uiState,
        onCodigoChange = viewModel::onCodigoChange,
        onIngresar = viewModel::onIngresar,
    )
}

@Composable
fun PilotoContent(
    uiState: PilotoUiState,
    onCodigoChange: (String) -> Unit,
    onIngresar: () -> Unit,
) {
    val brand = TrackingTheme.brand
    Surface(color = brand.header, modifier = Modifier.fillMaxSize()) {
        Box(
            Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .imePadding()
                .verticalScroll(rememberScrollState()),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 420.dp)
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                Icon(Icons.Outlined.Inventory2, contentDescription = null, tint = brand.accion, modifier = Modifier.size(64.dp))
                Text("CARGO EXPRESO", style = MaterialTheme.typography.displaySmall, color = brand.onHeader)
                Text(
                    "Módulo de tracking para pilotos",
                    style = MaterialTheme.typography.bodyLarge,
                    color = brand.onHeader.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center,
                )
                Card(
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                ) {
                    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text("INGRESA TU CÓDIGO DE PILOTO", style = MaterialTheme.typography.titleMedium)
                        OutlinedTextField(
                            value = uiState.codigo,
                            onValueChange = onCodigoChange,
                            label = { Text("Código de piloto") },
                            placeholder = { Text("PIL-001") },
                            leadingIcon = { Icon(Icons.Outlined.Badge, contentDescription = null) },
                            isError = uiState.error != null,
                            supportingText = { Text(uiState.error ?: "Ejemplo: PIL-001 o 1") },
                            singleLine = true,
                            shape = MaterialTheme.shapes.small,
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Characters,
                                imeAction = ImeAction.Done,
                            ),
                            keyboardActions = KeyboardActions(onDone = { onIngresar() }),
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Button(
                            onClick = onIngresar,
                            enabled = uiState.codigo.isNotBlank(),
                            shape = MaterialTheme.shapes.small,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = brand.accion,
                                contentColor = brand.onAccion,
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 52.dp),
                        ) {
                            Text("INGRESAR")
                        }
                    }
                }
            }
        }
    }
}
