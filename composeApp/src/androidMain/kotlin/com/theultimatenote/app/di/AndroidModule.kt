package com.theultimatenote.app.di

import com.theultimatenote.app.AndroidAppContext
import com.theultimatenote.app.BuildConfig
import com.theultimatenote.app.data.repository.AuthRepository
import com.theultimatenote.app.data.repository.ChatRepository
import com.theultimatenote.app.data.repository.FirebaseAuthRepository
import com.theultimatenote.app.data.repository.FirebaseChatRepository
import com.theultimatenote.app.data.repository.FirebaseNotebookRepository
import com.theultimatenote.app.data.repository.FirebaseProjectRepository
import com.theultimatenote.app.data.repository.FirebaseTaskRepository
import com.theultimatenote.app.data.repository.FirebaseUserRepository
import com.theultimatenote.app.data.repository.AiService
import com.theultimatenote.app.data.repository.FirebasePomodoroRepository
import com.theultimatenote.app.data.repository.GoogleCalendarSyncService
import com.theultimatenote.app.data.repository.NotebookRepository
import com.theultimatenote.app.data.repository.NotificationScheduler
import com.theultimatenote.app.data.repository.PomodoroRepository
import com.theultimatenote.app.data.repository.ProjectRepository
import com.theultimatenote.app.data.repository.SubscriptionRepository
import com.theultimatenote.app.data.repository.FirebaseSubscriptionRepository
import com.theultimatenote.app.data.repository.BillingManager
import com.theultimatenote.app.data.repository.AndroidBillingManager
import com.theultimatenote.app.data.repository.ImageStorageRepository
import com.theultimatenote.app.data.repository.FirebaseImageStorageRepository
import com.theultimatenote.app.data.repository.TaskRepository
import com.theultimatenote.app.data.repository.UserRepository
import com.theultimatenote.app.notifications.AndroidNotificationScheduler
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformModule(): Module = module {
    single<AuthRepository> { FirebaseAuthRepository() }
    single<ProjectRepository> { FirebaseProjectRepository() }
    single<TaskRepository> { FirebaseTaskRepository() }
    single<NotebookRepository> { FirebaseNotebookRepository() }
    single<UserRepository> { FirebaseUserRepository() }
    single<ChatRepository> { FirebaseChatRepository() }
    single { AiService(groqApiKey = BuildConfig.GROQ_API_KEY, geminiApiKeys = listOf(BuildConfig.GEMINI_API_KEY_1, BuildConfig.GEMINI_API_KEY_2)) }
    single<NotificationScheduler> { AndroidNotificationScheduler(AndroidAppContext.context) }
    single<PomodoroRepository> { FirebasePomodoroRepository() }
    single<SubscriptionRepository> { FirebaseSubscriptionRepository() }
    single<BillingManager> { AndroidBillingManager(AndroidAppContext.context, get()) }
    single<ImageStorageRepository> { FirebaseImageStorageRepository() }
    single { GoogleCalendarSyncService(AndroidAppContext.context) }
}
