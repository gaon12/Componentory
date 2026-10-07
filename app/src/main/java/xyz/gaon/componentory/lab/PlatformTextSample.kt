package xyz.gaon.componentory.lab

import android.widget.CheckedTextView
import android.widget.TextView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import xyz.gaon.componentory.R

@Composable
internal fun PlatformTextSample(
    family: PlatformFamily,
    component: LabComponent,
    viewId: Int,
    enabled: Boolean,
    state: SampleState,
    modifier: Modifier,
) {
    val panel = if (viewId == R.id.sample_left) "LEFT" else "RIGHT"
    var checkMarkMissing by remember { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ReadableAndroidView(
            factory = { context ->
                val themed = family.createContext(context)
                val view =
                    when (component) {
                        LabComponent.TEXT -> TextView(themed)
                        LabComponent.CHECKED_TEXT_VIEW ->
                            CheckedTextView(themed).apply {
                                // Use the selected theme's choice mark as the disclosed fixture.
                                val attributes =
                                    themed.obtainStyledAttributes(
                                        intArrayOf(android.R.attr.listChoiceIndicatorMultiple)
                                    )
                                try {
                                    val mark = attributes.getDrawable(0)
                                    setCheckMarkDrawable(mark)
                                    checkMarkMissing = mark == null
                                } finally {
                                    attributes.recycle()
                                }
                            }
                        else -> error("No framework text sample for ${component.name}")
                    }
                view.apply {
                    id = viewId
                    setText(R.string.sample_display_text)
                }
            },
            update = { view ->
                view.isEnabled = enabled
                if (view is CheckedTextView) view.isChecked = state.value == 1
            },
            onReset = null,
            modifier = modifier.testTag("native_$panel"),
        )
        if (checkMarkMissing) {
            Text(
                stringResource(R.string.checked_text_missing_mark),
                modifier = Modifier.testTag("checked_text_missing_mark_$panel"),
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}
