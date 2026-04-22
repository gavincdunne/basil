package org.weekendware.basil.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import org.weekendware.basil.data.local.database.DatabaseProvider
import org.weekendware.basil.data.repository.LogRepository
import org.weekendware.basil.data.repository.PreferencesRepository
import org.weekendware.basil.data.repository.SqlDelightLogRepository
import org.weekendware.basil.data.repository.SqlDelightPreferencesRepository
import org.weekendware.basil.data.repository.SqlDelightUserRepository
import org.weekendware.basil.data.repository.UserRepository
import org.weekendware.basil.domain.usecase.DeleteLogEntryUseCase
import org.weekendware.basil.domain.usecase.GetBgUnitPreferenceUseCase
import org.weekendware.basil.domain.usecase.GetLastBgReadingUseCase
import org.weekendware.basil.domain.usecase.GetTodayEntriesUseCase
import org.weekendware.basil.domain.usecase.ObserveRecentLogsUseCase
import org.weekendware.basil.domain.usecase.SaveLogEntryUseCase
import org.weekendware.basil.domain.usecase.SetBgUnitPreferenceUseCase
import org.weekendware.basil.presentation.chat.ChatViewModel
import org.weekendware.basil.presentation.dashboard.DashboardViewModel
import org.weekendware.basil.presentation.logging.LoggingViewModel
import org.weekendware.basil.presentation.profile.ProfileViewModel
import org.weekendware.basil.presentation.settings.SettingsViewModel

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
}

/**
 * Koin module that provides all shared ViewModels as singletons.
 */
val sharedModule = module {
    viewModel { DashboardViewModel(get(), get(), get()) }
    viewModel { ProfileViewModel() }
    viewModel { ChatViewModel() }
    viewModel { SettingsViewModel() }
    viewModel { LoggingViewModel(get(), get(), get()) }
}
