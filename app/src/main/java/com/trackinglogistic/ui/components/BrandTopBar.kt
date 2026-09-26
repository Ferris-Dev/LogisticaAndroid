package com.trackinglogistic.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.trackinglogistic.ui.theme.TrackingTheme

/** TopAppBar azul marino con la marca, como el header de cargoexpreso.com. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrandTopBar(
    subtitulo: String?,
    modifier: Modifier = Modifier,
    titulo: String = "CARGO EXPRESO",
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
) {
    val brand = TrackingTheme.brand
    TopAppBar(
        modifier = modifier,
        navigationIcon = navigationIcon,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Outlined.Inventory2,
                    contentDescription = null,
                    tint = brand.accion,
                    modifier = Modifier.size(26.dp),
                )
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(titulo, style = MaterialTheme.typography.titleLarge)
                    if (subtitulo != null) {
                        Text(
                            subtitulo,
                            style = MaterialTheme.typography.labelSmall,
                            color = brand.onHeader.copy(alpha = 0.75f),
                        )
                    }
                }
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = brand.header,
            titleContentColor = brand.onHeader,
            navigationIconContentColor = brand.onHeader,
            actionIconContentColor = brand.onHeader,
        ),
    )
}
