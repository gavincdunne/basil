package org.weekendware.basil.di

import org.koin.dsl.module
import org.weekendware.basil.data.local.database.DatabaseProvider
import org.weekendware.basil.data.repository.LogRepository
import org.weekendware.basil.data.repository.PreferencesRepository
import org.weekendware.basil.data.repository.UserRepository
import org.weekendware.basil.presentation.chat.ChatViewModel
import org.weekendware.basil.presentation.dashboard.DashboardViewModel
import org.weekendware.basil.presentation.logging.LoggingViewModel
import org.weekendware.basil.presentation.profile.ProfileViewModel
import org.weekendware.basil.presentation.settings.SettingsViewModel

/**
 * Koin module that wires the database and all repositories.
 *
 * - [BasilDatabase] is a singleton built from the platform-specific
 *   [DatabaseDriverFactory] (provided by [platformModule]).
 * - Repositories are singletons that each receive the shared database instance.
 */
val databaseModule = module {
    single { DatabaseProvider.getDatabase(get()) }
    single { UserRepository(get()) }
    single { LogRepository(get()) }
    single { PreferencesRepository(get()) }
}

/**
 * Koin module that provides all shared ViewModels as singletons.
 *
 * ViewModels are registered as `single` (not `viewModel`) so that they
 * are compatible across all KMP targets without requiring platform-specific
 * ViewModel lifecycle integration. Koin's `koinInject` is used at call sites
 * instead of `koinViewModel`.
 */
val sharedModule = module {
    single { DashboardViewModel() }
    single { ProfileViewModel() }
    single { ChatViewModel() }
    single { SettingsViewModel() }
    single { LoggingViewModel(get(), get()) }
}
