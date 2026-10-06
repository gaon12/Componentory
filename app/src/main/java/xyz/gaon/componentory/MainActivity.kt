package xyz.gaon.componentory

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import xyz.gaon.componentory.icons.IconCatalog
import xyz.gaon.componentory.navigation.ComponentoryApp
import xyz.gaon.componentory.settings.LanguagePreferences

class MainActivity : ComponentActivity() {
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LanguagePreferences.localizedContext(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // The ~11k-entry material index is parsed once and cached; warming it
        // here keeps the first icon-using panel and copy validation off the
        // main thread's critical path.
        lifecycleScope.launch(Dispatchers.IO) {
            IconCatalog.entries(applicationContext, platform = false)
        }
        setContent { ComponentoryApp() }
    }
}
