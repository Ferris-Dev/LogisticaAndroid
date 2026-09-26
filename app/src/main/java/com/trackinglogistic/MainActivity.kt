package com.trackinglogistic

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.trackinglogistic.ui.navigation.TrackingApp
import com.trackinglogistic.ui.piloto.PilotoScreen
import com.trackinglogistic.ui.theme.TrackingLogisticTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels()

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        super.onCreate(savedInstanceState)
        // El splash se mantiene hasta leer el piloto de DataStore, así no parpadea la pantalla de ingreso.
        splash.setKeepOnScreenCondition { mainViewModel.sesion.value == SesionState.Cargando }
        // El header siempre es azul marino: íconos de la barra de estado en blanco en ambos modos.
        enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT))

        setContent {
            TrackingLogisticTheme {
                val sesion by mainViewModel.sesion.collectAsStateWithLifecycle()
                val windowSizeClass = calculateWindowSizeClass(this)
                when (sesion) {
                    SesionState.Cargando -> Unit
                    SesionState.SinPiloto -> PilotoScreen()
                    is SesionState.ConPiloto -> TrackingApp(windowSizeClass.widthSizeClass)
                }
            }
        }
    }
}
