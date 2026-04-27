package org.weekendware.basil

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import org.koin.dsl.module
import org.weekendware.basil.crash.initSentry
import org.weekendware.basil.di.initKoin

/**
 * The single Android [ComponentActivity] that hosts the entire Basil UI.
 *
 * Responsibilities:
 * - Enables edge-to-edge display so content draws behind system bars.
 * - Initialises Koin, passing `applicationContext` as a Koin singleton so
 *   that [DatabaseDriverFactory] can receive it via `get<Context>()`.
 * - Sets the Compose content root to [App].
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        initSentry()
        initKoin {
            modules(module { single<android.content.Context> { applicationContext } })
        }

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
