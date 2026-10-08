package xyz.gaon.componentory.survivor

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.remember
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
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
        // The arena also draws beside a camera cutout; the HUD keeps cutout padding.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
            window.attributes.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        hideSystemBars()
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

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        // A swipe, dialog, or notification shade can reveal the bars; hide them again.
        if (hasFocus) hideSystemBars()
    }

    private fun hideSystemBars() {
        WindowCompat.getInsetsController(window, window.decorView).apply {
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            hide(WindowInsetsCompat.Type.systemBars())
        }
    }
}
