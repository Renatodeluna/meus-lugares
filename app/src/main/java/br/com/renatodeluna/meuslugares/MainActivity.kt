package br.com.renatodeluna.meuslugares

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import br.com.renatodeluna.meuslugares.ui.navigation.MeusLugaresNavHost
import br.com.renatodeluna.meuslugares.ui.theme.MeusLugaresTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MeusLugaresTheme {
                // Cada tela tem o próprio Scaffold (TopAppBar e FAB variam por tela);
                // um Scaffold aqui em volta duplicaria os insets do sistema.
                MeusLugaresNavHost()
            }
        }
    }
}
