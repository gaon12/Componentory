package xyz.gaon.componentory.lab.inline

import android.app.Activity
import android.content.Intent
import android.widget.Button
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import xyz.gaon.componentory.R
import xyz.gaon.componentory.lab.PlatformFamily
import xyz.gaon.componentory.lab.ReadableAndroidView
import xyz.gaon.componentory.lab.SampleState

@RequiresApi(30)
@Composable
internal fun InlineDemoLauncher(
    family: PlatformFamily,
    viewId: Int,
    enabled: Boolean,
    state: SampleState,
    modifier: Modifier,
) {
    val context = LocalContext.current
    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result
            ->
            if (result.resultCode == Activity.RESULT_OK)
                result.data?.let { data ->
                    state.value =
                        if (data.getBooleanExtra(InlineDemoActivity.EXTRA_INFLATED, false)) 1 else 0
                }
        }
    ReadableAndroidView(
        factory = {
            Button(family.createContext(it)).apply {
                id = viewId
                setText(R.string.inline_open)
            }
        },
        update = { button ->
            button.isEnabled = enabled
            button.setOnClickListener {
                launcher.launch(
                    Intent(context, InlineDemoActivity::class.java)
                        .putExtra(InlineDemoActivity.EXTRA_FAMILY, family.name)
                        .putExtra(InlineDemoActivity.EXTRA_INFLATED, state.value > 0)
                )
            }
        },
        modifier =
            modifier.testTag(if (viewId == R.id.sample_left) "native_LEFT" else "native_RIGHT"),
    )
}
