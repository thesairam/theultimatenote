package com.theultimatenote.app

import androidx.compose.runtime.Composable
import com.theultimatenote.app.di.appModule
import com.theultimatenote.app.di.platformModule
import com.theultimatenote.app.ui.navigation.AppNavigation
import com.theultimatenote.app.ui.theme.UltimateNoteTheme
import org.koin.compose.KoinApplication

@Composable
fun App() {
    KoinApplication(application = {
        modules(platformModule(), appModule)
    }) {
        UltimateNoteTheme {
            AppNavigation()
        }
    }
}
