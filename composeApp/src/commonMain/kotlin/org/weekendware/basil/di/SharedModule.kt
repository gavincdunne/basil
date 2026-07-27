package org.weekendware.basil.di

import com.russhwolf.settings.Settings
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import org.weekendware.basil.data.local.database.DatabaseProvider
import org.weekendware.basil.data.remote.createSupabaseClient
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.weekendware.basil.data.repository.AuthRepository
import org.weekendware.basil.data.repository.AvatarRepository
import org.weekendware.basil.data.repository.ChatRepository
import org.weekendware.basil.data.repository.DataStoreOnboardingRepository
import org.weekendware.basil.data.repository.KtorChatRepository
import org.weekendware.basil.data.repository.OnboardingLocalRepository
import org.weekendware.basil.data.repository.ProfileRepository
import org.weekendware.basil.data.repository.SqlDelightUserRepository
import org.weekendware.basil.data.repository.SupabaseAuthRepository
import org.weekendware.basil.data.repository.SupabaseAvatarRepository
import org.weekendware.basil.data.repository.SupabaseProfileRepository
import org.weekendware.basil.data.repository.UserRepository
import org.weekendware.basil.domain.usecase.GetUserUseCase
import org.weekendware.basil.domain.usecase.SendMessageUseCase
import org.weekendware.basil.presentation.auth.AuthViewModel
import org.weekendware.basil.presentation.auth.NewPasswordViewModel
import org.weekendware.basil.presentation.auth.ResetPasswordViewModel
import org.weekendware.basil.presentation.auth.VerificationWallViewModel
import org.weekendware.basil.presentation.chat.ChatViewModel
import org.weekendware.basil.presentation.onboarding.OnboardingViewModel
import org.weekendware.basil.presentation.onboarding.SaveProgressViewModel
import org.weekendware.basil.presentation.profile.ProfileViewModel
import org.weekendware.basil.presentation.session.SessionViewModel
import org.weekendware.basil.presentation.settings.SettingsViewModel
import org.weekendware.basil.presentation.theme.BasilThemeViewModel

val chatModule = module {
    single {
        HttpClient {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
    }
    single<ChatRepository> { KtorChatRepository(get()) }
}

val supabaseModule = module {
    single { createSupabaseClient() }
    single { Settings() }
    single<AuthRepository> { SupabaseAuthRepository(get(), get()) }
    single<AvatarRepository> { SupabaseAvatarRepository(get()) }
}

val databaseModule = module {
    single { DatabaseProvider.getDatabase(get()) }
    single<UserRepository> { SqlDelightUserRepository(get()) }
}

val useCaseModule = module {
    single { GetUserUseCase(get()) }
    single { SendMessageUseCase(get()) }
}

val onboardingModule = module {
    single<OnboardingLocalRepository> { DataStoreOnboardingRepository(get()) }
    single<ProfileRepository> { SupabaseProfileRepository(get()) }
}

val sharedModule = module {
    viewModel { SessionViewModel(get()) }
    viewModel { AuthViewModel(get()) }
    viewModel { ResetPasswordViewModel(get()) }
    viewModel { NewPasswordViewModel(get()) }
    viewModel { VerificationWallViewModel(get()) }
    viewModel { OnboardingViewModel(get(), get(), get(), get()) }
    viewModel { SaveProgressViewModel(get()) }
    viewModel { ProfileViewModel(get(), get(), get()) }
    viewModel { ChatViewModel(get(), get()) }
    viewModel { SettingsViewModel() }
    viewModel { BasilThemeViewModel() }
}
