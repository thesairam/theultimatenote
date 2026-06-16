package com.theultimatenote.app.di

import com.theultimatenote.app.ui.screens.auth.AuthViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

expect fun platformModule(): org.koin.core.module.Module

val appModule = module {
    viewModelOf(::AuthViewModel)
}
