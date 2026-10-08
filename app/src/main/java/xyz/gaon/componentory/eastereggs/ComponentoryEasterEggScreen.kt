package xyz.gaon.componentory.eastereggs

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import xyz.gaon.componentory.survivor.SurvivorActivity

/** Keep Settings state in the parent while the private landscape Activity runs. */
@Composable
internal fun ComponentoryEasterEggScreen(onClose: () -> Unit) {
    val context = LocalContext.current
    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            onClose()
        }
    var launched by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (!launched) {
            launched = true
            launcher.launch(Intent(context, SurvivorActivity::class.java))
        }
    }
}
