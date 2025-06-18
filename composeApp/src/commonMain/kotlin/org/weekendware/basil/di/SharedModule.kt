package org.weekendware.basil.di

import org.koin.dsl.module
import org.weekendware.basil.presentation.chat.ChatViewModel
import org.weekendware.basil.presentation.dashboard.DashboardViewModel
import org.weekendware.basil.presentation.profile.ProfileViewModel
import org.weekendware.basil.presentation.settings.SettingsViewModel

val sharedModule = module {
    single { DashboardViewModel() }
    single { ProfileViewModel() }
    single { ChatViewModel() }
    single { SettingsViewModel() }
}