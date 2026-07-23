package org.weekendware.basil

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

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
