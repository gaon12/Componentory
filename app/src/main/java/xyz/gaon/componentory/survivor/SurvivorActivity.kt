package xyz.gaon.componentory.survivor

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.remember
import xyz.gaon.componentory.settings.AppAppearance
import xyz.gaon.componentory.settings.AppearancePreferences
import xyz.gaon.componentory.settings.LanguagePreferences
import xyz.gaon.componentory.ui.theme.ComponentoryTheme

class SurvivorActivity : ComponentActivity() {
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LanguagePreferences.localizedContext(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val appearance = remember { AppearancePreferences(this).read() }
            val assets = remember { GameAssets(this) }
            val dark =
                when (appearance) {
                    AppAppearance.SYSTEM -> isSystemInDarkTheme()
                    AppAppearance.LIGHT -> false
                    AppAppearance.DARK -> true
                }
            ComponentoryTheme(darkTheme = dark, dynamicColor = false) {
                GameHost(assets, onClose = ::finish)
            }
        }
    }
}
