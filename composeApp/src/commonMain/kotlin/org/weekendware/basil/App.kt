package org.weekendware.basil

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import basil.composeapp.generated.resources.Res
import basil.composeapp.generated.resources.cd_close
import basil.composeapp.generated.resources.cd_settings
import basil.composeapp.generated.resources.chat_return_day
import basil.composeapp.generated.resources.chat_return_evening
import basil.composeapp.generated.resources.chat_return_morning
import basil.composeapp.generated.resources.chat_return_night
import basil.composeapp.generated.resources.nav_chat
import basil.composeapp.generated.resources.nav_more
import basil.composeapp.generated.resources.nav_profile
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.weekendware.basil.presentation.auth.AuthScreen
import org.weekendware.basil.presentation.auth.VerificationWallScreen
import org.weekendware.basil.presentation.chat.ChatScreen
import org.weekendware.basil.presentation.more.MoreScreen
import org.weekendware.basil.presentation.onboarding.OnboardingScreen
import org.weekendware.basil.presentation.onboarding.OnboardingViewModel
import org.weekendware.basil.presentation.profile.ProfileScreen
import org.weekendware.basil.presentation.session.AuthenticatedDestination
import org.weekendware.basil.presentation.session.SessionState
import org.weekendware.basil.presentation.session.SessionViewModel
import org.weekendware.basil.presentation.session.authenticatedDestination
import org.weekendware.basil.presentation.settings.SettingsScreen
import org.weekendware.basil.presentation.splash.SplashScreen
import org.weekendware.basil.presentation.theme.BasilTheme
import org.weekendware.basil.presentation.theme.BasilThemeViewModel
import org.weekendware.basil.presentation.theme.backgroundBrush
import org.weekendware.basil.presentation.theme.basilColors
import org.weekendware.basil.presentation.theme.dmSerifDisplayFamily

enum class BasilTab { Profile, Chat, More }

fun shouldShowSplash(sessionState: SessionState, splashFadeDone: Boolean): Boolean =
    sessionState == SessionState.Loading || !splashFadeDone

@Composable
fun App() {
    val sessionViewModel = koinViewModel<SessionViewModel>()
    val sessionState by sessionViewModel.state.collectAsStateWithLifecycle()
    var splashDone by remember { mutableStateOf(false) }
    val showSplash = shouldShowSplash(sessionState, splashDone)

    val themeViewModel = koinViewModel<BasilThemeViewModel>()
    val themeHour by themeViewModel.hour.collectAsStateWithLifecycle()

    BasilTheme(hour = themeHour) {
        val bgBrush = MaterialTheme.basilColors.backgroundBrush()
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(bgBrush)
        ) {
            AnimatedContent(
                targetState  = showSplash,
                transitionSpec = {
                    fadeIn(tween(400)) togetherWith fadeOut(tween(0))
                },
                label = "splash_to_content",
            ) { splashVisible ->
                if (splashVisible) {
                    SplashScreen(onFadeComplete = { splashDone = true })
                } else {
                    when (val session = sessionState) {
                        SessionState.Unauthenticated -> AuthScreen()
                        is SessionState.Authenticated -> when (authenticatedDestination(session)) {
                            AuthenticatedDestination.Normal ->
                                AuthenticatedRoot(themeHour = themeHour)
                            AuthenticatedDestination.VerificationWall ->
                                VerificationWallScreen()
                        }
                        SessionState.Loading -> Box(Modifier.fillMaxSize())
                    }
                }
            }
        }
    }
}

@Composable
private fun AuthenticatedRoot(themeHour: Int) {
    val hour = themeHour

    val onboardingViewModel = koinViewModel<OnboardingViewModel>()
    val onboardingState by onboardingViewModel.state.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableStateOf(BasilTab.Chat) }
    var isSettingsOpen by remember { mutableStateOf(false) }
    val name = onboardingState.name ?: ""
    val returnGreeting = if (onboardingState.isComplete) {
        when (hour) {
            in 5..9   -> stringResource(Res.string.chat_return_morning, name)
            in 10..17 -> stringResource(Res.string.chat_return_day)
            in 18..20 -> stringResource(Res.string.chat_return_evening, name)
            else      -> stringResource(Res.string.chat_return_night)
        }
    } else null

    Column(modifier = Modifier.fillMaxSize()) {
        BasilTopBar(
            isSettingsOpen   = isSettingsOpen,
            onToggleSettings = { isSettingsOpen = !isSettingsOpen },
        )

        Box(modifier = Modifier.weight(1f)) {
            AnimatedContent(
                targetState  = selectedTab,
                transitionSpec = {
                    val direction = if (targetState.ordinal > initialState.ordinal) 1 else -1
                    slideInHorizontally(tween(300)) { it * direction } togetherWith
                        slideOutHorizontally(tween(300)) { -it * direction }
                },
                label    = "tab_content",
                modifier = Modifier.fillMaxSize(),
            ) { tab ->
                when (tab) {
                    BasilTab.Profile -> ProfileScreen()
                    BasilTab.Chat    -> when {
                        onboardingState.isLoading && !onboardingState.isComplete ->
                            Box(Modifier.fillMaxSize())
                        onboardingState.isComplete && !onboardingState.completedThisSession ->
                            ChatScreen(initialGreeting = returnGreeting)
                        else ->
                            OnboardingScreen(
                                viewModel = onboardingViewModel,
                                modifier  = Modifier.fillMaxSize(),
                            )
                    }
                    BasilTab.More    -> MoreScreen()
                }
            }

            // Settings overlay: graphicsLayer offset avoids AnimatedVisibility scope issues
            // in nested Box/Column context. 0f = on screen, 1f = fully below screen.
            val settingsSlide by animateFloatAsState(
                targetValue   = if (isSettingsOpen) 0f else 1f,
                animationSpec = tween(if (isSettingsOpen) 350 else 280),
                label         = "settings_slide",
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { translationY = size.height * settingsSlide }
                    .background(MaterialTheme.basilColors.backgroundBrush())
            ) {
                SettingsScreen()
            }
        }

        // ColumnScope.AnimatedVisibility — valid here as a direct Column child
        AnimatedVisibility(
            visible = onboardingState.isComplete,
            enter   = fadeIn(tween(600)),
        ) {
            BasilNavBar(
                selectedTab   = selectedTab,
                onTabSelected = { selectedTab = it },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BasilTopBar(
    isSettingsOpen: Boolean = false,
    onToggleSettings: () -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        CenterAlignedTopAppBar(
            title = {
                Text(
                    text          = "basil",
                    fontFamily    = dmSerifDisplayFamily(),
                    fontWeight    = FontWeight.Normal,
                    fontSize      = 22.sp,
                    letterSpacing = (-0.01).sp,
                    color         = MaterialTheme.colorScheme.onSurface,
                )
            },
            actions = {
                IconButton(onClick = onToggleSettings) {
                    Crossfade(
                        targetState   = isSettingsOpen,
                        animationSpec = tween(250),
                        label         = "cog_x",
                    ) { open ->
                        if (open) {
                            Icon(
                                imageVector        = Icons.Default.Close,
                                contentDescription = stringResource(Res.string.cd_close),
                                tint               = MaterialTheme.colorScheme.primary,
                            )
                        } else {
                            Icon(
                                imageVector        = Icons.Default.Settings,
                                contentDescription = stringResource(Res.string.cd_settings),
                                tint               = MaterialTheme.colorScheme.primary.copy(alpha = 0.65f),
                            )
                        }
                    }
                }
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor    = MaterialTheme.colorScheme.surface,
                titleContentColor = MaterialTheme.colorScheme.onSurface,
            ),
            expandedHeight = 52.dp,
        )
        HorizontalDivider(
            color     = MaterialTheme.colorScheme.outlineVariant,
            thickness = 1.dp
        )
    }
}

@Composable
private fun BasilNavBar(
    selectedTab: BasilTab,
    onTabSelected: (BasilTab) -> Unit,
) {
    val itemColors = NavigationBarItemDefaults.colors(
        indicatorColor      = MaterialTheme.colorScheme.primaryContainer,
        selectedIconColor   = MaterialTheme.colorScheme.primary,
        selectedTextColor   = MaterialTheme.colorScheme.primary,
        unselectedIconColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.65f),
        unselectedTextColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.80f),
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        HorizontalDivider(
            color     = MaterialTheme.colorScheme.outlineVariant,
            thickness = 1.dp
        )
        NavigationBar(
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp,
        ) {
            NavigationBarItem(
                selected = selectedTab == BasilTab.Profile,
                onClick  = { onTabSelected(BasilTab.Profile) },
                icon     = { Icon(Icons.Default.Person, contentDescription = null) },
                label    = { Text(stringResource(Res.string.nav_profile)) },
                colors   = itemColors,
            )
            NavigationBarItem(
                selected = selectedTab == BasilTab.Chat,
                onClick  = { onTabSelected(BasilTab.Chat) },
                icon     = { Icon(Icons.Default.ChatBubble, contentDescription = null) },
                label    = { Text(stringResource(Res.string.nav_chat)) },
                colors   = itemColors,
            )
            NavigationBarItem(
                selected = selectedTab == BasilTab.More,
                onClick  = { onTabSelected(BasilTab.More) },
                icon     = { Icon(Icons.Default.MoreHoriz, contentDescription = null) },
                label    = { Text(stringResource(Res.string.nav_more)) },
                colors   = itemColors,
            )
        }
    }
}
