package org.weekendware.basil.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import org.weekendware.basil.data.local.database.DatabaseProvider
import org.weekendware.basil.data.remote.createSupabaseClient
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.weekendware.basil.data.repository.AuthRepository
import org.weekendware.basil.data.repository.ChatRepository
import org.weekendware.basil.data.repository.KtorChatRepository
import org.weekendware.basil.data.repository.LogRepository
import org.weekendware.basil.data.repository.PreferencesRepository
import org.weekendware.basil.data.repository.SqlDelightLogRepository
import org.weekendware.basil.data.repository.SqlDelightPreferencesRepository
import org.weekendware.basil.data.repository.SqlDelightUserRepository
import org.weekendware.basil.data.repository.SupabaseAuthRepository
import org.weekendware.basil.data.repository.UserRepository
import org.weekendware.basil.domain.usecase.DeleteLogEntryUseCase
import org.weekendware.basil.domain.usecase.SendMessageUseCase
import org.weekendware.basil.domain.usecase.GetBgTargetsUseCase
import org.weekendware.basil.domain.usecase.GetBgUnitPreferenceUseCase
import org.weekendware.basil.domain.usecase.GetLastBgReadingUseCase
import org.weekendware.basil.domain.usecase.GetTodayEntriesUseCase
import org.weekendware.basil.domain.usecase.GetUserUseCase
import org.weekendware.basil.domain.usecase.ObserveRecentLogsUseCase
import org.weekendware.basil.domain.usecase.SaveLogEntryUseCase
import org.weekendware.basil.domain.usecase.SetBgTargetsUseCase
import org.weekendware.basil.domain.usecase.SetBgUnitPreferenceUseCase
import org.weekendware.basil.presentation.auth.AuthViewModel
import org.weekendware.basil.presentation.chat.ChatViewModel
import org.weekendware.basil.presentation.dashboard.DashboardViewModel
import org.weekendware.basil.presentation.logging.LoggingViewModel
import org.weekendware.basil.presentation.profile.ProfileViewModel
import org.weekendware.basil.presentation.session.SessionViewModel
import org.weekendware.basil.presentation.settings.SettingsViewModel

/**
 * Koin module that provides the shared Ktor [HttpClient] and the chat
 * repository backed by the basil-chat-api SSE endpoint.
 */
val chatModule = module {
    // Single HttpClient instance shared across all network calls.
    // ContentNegotiation handles JSON serialisation for request bodies.
    single {
        HttpClient {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
    }
    single<ChatRepository> { KtorChatRepository(get()) }
}

/**
 * Koin module that wires the Supabase client and auth repository.
 */
val supabaseModule = module {
    single { createSupabaseClient() }
    single<AuthRepository> { SupabaseAuthRepository(get()) }
}

/**
 * Koin module that wires the database and all repositories.
 */
val databaseModule = module {
    single { DatabaseProvider.getDatabase(get()) }
    single<UserRepository> { SqlDelightUserRepository(get()) }
    single<LogRepository> { SqlDelightLogRepository(get()) }
    single<PreferencesRepository> { SqlDelightPreferencesRepository(get()) }
}

/**
 * Koin module that provides all use cases as singletons.
 */
val useCaseModule = module {
    single { ObserveRecentLogsUseCase(get()) }
    single { GetTodayEntriesUseCase() }
    single { GetLastBgReadingUseCase() }
    single { SaveLogEntryUseCase(get()) }
    single { DeleteLogEntryUseCase(get()) }
    single { GetBgUnitPreferenceUseCase(get()) }
    single { SetBgUnitPreferenceUseCase(get()) }
    single { GetUserUseCase(get()) }
    single { GetBgTargetsUseCase(get()) }
    single { SetBgTargetsUseCase(get()) }
    single { SendMessageUseCase(get()) }
}

/**
 * Koin module that provides all shared ViewModels as singletons.
 */
val sharedModule = module {
    viewModel { SessionViewModel(get()) }
    viewModel { AuthViewModel(get()) }
    viewModel { DashboardViewModel(get(), get(), get()) }
    viewModel { ProfileViewModel(get(), get(), get()) }
    viewModel { ChatViewModel(get()) }
    viewModel { SettingsViewModel(get(), get()) }
    viewModel { LoggingViewModel(get(), get(), get()) }
}
