package xyz.gaon.componentory.lab

import android.app.Activity
import android.content.Intent
import android.widget.Button
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import xyz.gaon.componentory.R

@Composable
internal fun ActionBarSampleLauncher(
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
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.let { data ->
                    state.value =
                        data.getIntExtra(ActionBarSampleActivity.EXTRA_CLICKS, state.value)
                    state.text =
                        if (data.getBooleanExtra(ActionBarSampleActivity.EXTRA_VISIBLE, true))
                            "visible"
                        else "hidden"
                }
            }
        }
    ReadableAndroidView(
        factory = {
            Button(family.createContext(it)).apply {
                id = viewId
                setText(R.string.action_bar_open)
            }
        },
        update = { button ->
            button.isEnabled = enabled
            button.setOnClickListener {
                launcher.launch(
                    Intent(context, ActionBarSampleActivity::class.java)
                        .putExtra(ActionBarSampleActivity.EXTRA_FAMILY, family.name)
                        .putExtra(ActionBarSampleActivity.EXTRA_CLICKS, state.value)
                        .putExtra(ActionBarSampleActivity.EXTRA_VISIBLE, state.text != "hidden")
                )
            }
        },
        modifier =
            modifier.testTag(if (viewId == R.id.sample_left) "native_LEFT" else "native_RIGHT"),
    )
}
