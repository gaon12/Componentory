package xyz.gaon.componentory.lab

import android.text.Editable
import android.text.TextWatcher
import android.view.ContextThemeWrapper
import android.view.ViewGroup
import android.widget.DialerFilter
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.children

@Suppress("DEPRECATION")
@Composable
internal fun PlatformDialerSample(
    family: PlatformFamily,
    component: LabComponent,
    viewId: Int,
    enabled: Boolean,
    state: SampleState,
    modifier: Modifier,
) {
    AndroidView(
        factory = { context ->
            val themed = ContextThemeWrapper(context, family.themeId)
            DialerFilter(themed).apply {
                id = viewId
                mode = DialerFilter.DIGITS_AND_LETTERS
            }
        },
        update = { filter ->
            // setFilterWatcher reports every real keystroke the widget filters,
            // so the copied text is the composed filter, not a faked edit field.
            filter.setFilterWatcher(
                object : TextWatcher {
                    override fun beforeTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}

                    override fun onTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}

                    override fun afterTextChanged(s: Editable?) {
                        state.text = filter.filterText.toString()
                    }
                }
            )
            if (filter.filterText.toString() != state.text) {
                filter.clearText()
                filter.append(state.text)
            }
            (filter as ViewGroup).children.forEach { it.isEnabled = enabled }
            filter.isEnabled = enabled
        },
        modifier = modifier.testTag("sample-${component.name.lowercase()}"),
    )
}
