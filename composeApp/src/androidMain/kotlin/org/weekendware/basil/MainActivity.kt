package org.weekendware.basil

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.dsl.module
import org.koin.mp.KoinPlatform
import org.weekendware.basil.crash.initSentry
import org.weekendware.basil.data.repository.AuthRepository
import org.weekendware.basil.di.initKoin

/**
 * The single Android [ComponentActivity] that hosts the entire Basil UI.
 *
 * Responsibilities:
 * - Installs the OS-level SplashScreen and keeps it visible until the Supabase
 *   SDK has resolved the stored session. This covers the cold-start window
 *   before the first Compose frame is drawn.
 * - Enables edge-to-edge display so content draws behind system bars.
 * - Initialises Koin, passing `applicationContext` so [DatabaseDriverFactory]
 *   can receive it via `get<Context>()`.
 * - Sets the Compose content root to [App].
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        initSentry()
        initKoin {
            modules(module { single<android.content.Context> { applicationContext } })
        }

        // Keep the OS splash visible until sessionFlow emits its first value.
        // The first emission means Supabase has finished restoring (or not) the
        // stored session — at that point the Compose layer is ready to show the
        // correct screen and the OS splash can exit.
        var sessionResolved = false
        val authRepository = KoinPlatform.getKoin().get<AuthRepository>()
        lifecycleScope.launch {
            authRepository.sessionFlow.first()
            sessionResolved = true
        }
        splashScreen.setKeepOnScreenCondition { !sessionResolved }

        setContent {
            App()
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
