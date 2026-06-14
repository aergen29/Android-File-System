package com.example.filedemo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.filedemo.screen.NavControllerScreen
import com.example.filedemo.ui.theme.FileDemoTheme

val LocalSnackbarProvider = staticCompositionLocalOf<SnackbarHostState> {
    error("No SnackbarHostState provided")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {

            val context = LocalContext.current
            val snackbarHostState = remember { SnackbarHostState() }
            FileDemoTheme {
                CompositionLocalProvider(LocalSnackbarProvider provides snackbarHostState) {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        snackbarHost = { SnackbarHost(LocalSnackbarProvider.current) }
                    ) { innerPadding ->
                        NavControllerScreen(context).getScreen(Modifier.padding(innerPadding))
                    }
                }
            }
        }
    }
}
