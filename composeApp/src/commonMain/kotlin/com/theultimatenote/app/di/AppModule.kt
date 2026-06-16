package com.theultimatenote.app.di

import com.theultimatenote.app.ui.screens.auth.AuthViewModel
import com.theultimatenote.app.ui.screens.daily.DailyViewModel
import com.theultimatenote.app.ui.screens.projects.KanbanViewModel
import com.theultimatenote.app.ui.screens.projects.ProjectsViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

expect fun platformModule(): org.koin.core.module.Module

val appModule = module {
    viewModelOf(::AuthViewModel)
    viewModelOf(::ProjectsViewModel)
    viewModelOf(::DailyViewModel)
    factory { (projectId: String) -> KanbanViewModel(projectId, get(), get()) }
}
