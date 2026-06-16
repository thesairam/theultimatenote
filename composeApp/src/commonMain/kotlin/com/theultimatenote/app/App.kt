package com.theultimatenote.app

import androidx.compose.runtime.Composable
import com.theultimatenote.app.ui.navigation.AppNavigation
import com.theultimatenote.app.ui.theme.UltimateNoteTheme

@Composable
fun App() {
    UltimateNoteTheme {
        AppNavigation()
    }
}
