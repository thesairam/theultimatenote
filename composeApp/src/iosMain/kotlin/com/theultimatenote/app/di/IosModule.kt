package com.theultimatenote.app.di

import com.theultimatenote.app.BuildKeys
import com.theultimatenote.app.data.repository.AiService
import com.theultimatenote.app.data.repository.AuthRepository
import com.theultimatenote.app.data.repository.BillingManager
import com.theultimatenote.app.data.repository.ChatRepository
import com.theultimatenote.app.data.repository.ImageStorageRepository
import com.theultimatenote.app.data.repository.IosAuthRepository
import com.theultimatenote.app.data.repository.IosBillingManager
import com.theultimatenote.app.data.repository.IosChatRepository
import com.theultimatenote.app.data.repository.IosImageStorageRepository
import com.theultimatenote.app.data.repository.IosNotebookRepository
import com.theultimatenote.app.data.repository.IosPomodoroRepository
import com.theultimatenote.app.data.repository.IosProjectRepository
import com.theultimatenote.app.data.repository.IosSubscriptionRepository
import com.theultimatenote.app.data.repository.IosTaskRepository
import com.theultimatenote.app.data.repository.IosUserRepository
import com.theultimatenote.app.data.repository.NotebookRepository
import com.theultimatenote.app.data.repository.NotificationScheduler
import com.theultimatenote.app.data.repository.PomodoroRepository
import com.theultimatenote.app.data.repository.ProjectRepository
import com.theultimatenote.app.data.repository.SubscriptionRepository
import com.theultimatenote.app.data.repository.TaskRepository
import com.theultimatenote.app.data.repository.UserRepository
import com.theultimatenote.app.notifications.IosNotificationScheduler
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformModule(): Module = module {
    single<AuthRepository> { IosAuthRepository() }
    single<ProjectRepository> { IosProjectRepository() }
    single<TaskRepository> { IosTaskRepository() }
    single<NotebookRepository> { IosNotebookRepository() }
    single<UserRepository> { IosUserRepository() }
    single<ChatRepository> { IosChatRepository() }
    single {
        AiService(
            groqApiKey = BuildKeys.GROQ_API_KEY,
            geminiApiKeys = listOf(BuildKeys.GEMINI_API_KEY_1, BuildKeys.GEMINI_API_KEY_2),
        )
    }
    single<NotificationScheduler> { IosNotificationScheduler() }
    single<PomodoroRepository> { IosPomodoroRepository() }
    single<SubscriptionRepository> { IosSubscriptionRepository() }
    single<BillingManager> { IosBillingManager() }
    single<ImageStorageRepository> { IosImageStorageRepository() }
}
