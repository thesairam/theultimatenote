package com.theultimatenote.app.di

import com.theultimatenote.app.data.repository.AuthRepository
import com.theultimatenote.app.data.repository.FirebaseAuthRepository
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformModule(): Module = module {
    single<AuthRepository> { FirebaseAuthRepository() }
}
