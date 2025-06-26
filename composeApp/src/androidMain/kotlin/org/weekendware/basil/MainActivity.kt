package org.weekendware.basil

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.google.firebase.FirebaseApp
import org.weekendware.basil.di.initKoin

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        initKoin()
        FirebaseApp.initializeApp(this)

        setContent {
            App()
        }
    }
}