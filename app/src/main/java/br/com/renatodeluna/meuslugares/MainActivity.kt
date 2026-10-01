package br.com.renatodeluna.meuslugares

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import br.com.renatodeluna.meuslugares.ui.theme.MeusLugaresTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MeusLugaresTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // O NavHost entra aqui no próximo passo.
                    Box(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}
