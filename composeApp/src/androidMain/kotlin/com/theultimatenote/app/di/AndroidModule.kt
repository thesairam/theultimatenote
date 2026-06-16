package com.theultimatenote.app.di

import com.theultimatenote.app.data.repository.AuthRepository
import com.theultimatenote.app.data.repository.FirebaseAuthRepository
import com.theultimatenote.app.data.repository.FirebaseProjectRepository
import com.theultimatenote.app.data.repository.FirebaseTaskRepository
import com.theultimatenote.app.data.repository.ProjectRepository
import com.theultimatenote.app.data.repository.TaskRepository
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformModule(): Module = module {
    single<AuthRepository> { FirebaseAuthRepository() }
    single<ProjectRepository> { FirebaseProjectRepository() }
    single<TaskRepository> { FirebaseTaskRepository() }
}
