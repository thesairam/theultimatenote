package com.theultimatenote.app.di

import com.theultimatenote.app.ui.screens.auth.AuthViewModel
import com.theultimatenote.app.ui.screens.chat.ChatViewModel
import com.theultimatenote.app.ui.screens.daily.DailyViewModel
import com.theultimatenote.app.ui.screens.home.HomeViewModel
import com.theultimatenote.app.ui.screens.notebooks.NotebooksViewModel
import com.theultimatenote.app.ui.screens.profile.ProfileViewModel
import com.theultimatenote.app.ui.screens.projects.KanbanViewModel
import com.theultimatenote.app.ui.screens.projects.ProjectsViewModel
import com.theultimatenote.app.ui.screens.stats.StatsViewModel
import com.theultimatenote.app.ui.screens.subscription.SubscriptionViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

expect fun platformModule(): org.koin.core.module.Module

val appModule = module {
    viewModelOf(::AuthViewModel)
    viewModelOf(::ProjectsViewModel)
    viewModelOf(::DailyViewModel)
    viewModelOf(::HomeViewModel)
    viewModelOf(::NotebooksViewModel)
    viewModelOf(::ProfileViewModel)
    viewModelOf(::ChatViewModel)
    viewModelOf(::StatsViewModel)
    viewModelOf(::SubscriptionViewModel)
    factory { (projectId: String) -> KanbanViewModel(projectId, get(), get()) }
}
