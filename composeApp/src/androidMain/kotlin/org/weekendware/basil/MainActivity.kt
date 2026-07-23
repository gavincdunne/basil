package org.weekendware.basil

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import org.koin.mp.KoinPlatform
import org.weekendware.basil.data.repository.DeepLinkHandler

/**
 * The single Android [ComponentActivity] that hosts the entire Basil UI.
 *
 * Responsibilities:
 * - Installs the OS-level SplashScreen as a functional pass-through — it exits
 *   on the first drawn frame and makes no branded statement of its own. The
 *   Compose [org.weekendware.basil.presentation.splash.SplashScreen] is the
 *   single source of truth for the branded splash moment, including the wait
 *   for session restoration. Holding the OS splash on session resolution here
 *   as well would draw two splashes back-to-back.
 * - Enables edge-to-edge display so content draws behind system bars.
 * - Sets the Compose content root to [App].
 * - Forwards incoming `basil://` deep links (password reset, OAuth
 *   callback) to [DeepLinkHandler] — both a cold-start launch via
 *   [onCreate]'s intent and a warm relaunch via [onNewIntent]
 *   (`launchMode="singleTop"` in the manifest routes the latter here
 *   instead of spawning a new activity instance).
 *
 * Koin and Sentry initialisation live in [BasilApplication.onCreate] so they
 * survive activity recreation (rotation, multi-window, etc.) without throwing
 * [org.koin.core.error.KoinApplicationAlreadyStartedException].
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        handleDeepLink(intent)

        setContent {
            App()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleDeepLink(intent)
    }

    private fun handleDeepLink(intent: Intent?) {
        val url = intent?.data?.toString() ?: return
        val deepLinkHandler = KoinPlatform.getKoin().get<DeepLinkHandler>()
        lifecycleScope.launch {
            deepLinkHandler.handle(url)
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
