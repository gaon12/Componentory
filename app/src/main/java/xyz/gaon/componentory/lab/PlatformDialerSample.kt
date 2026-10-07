package xyz.gaon.componentory.lab

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.DialerFilter
import android.widget.EditText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.children
import xyz.gaon.componentory.R

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
            val themed = family.createContext(context)
            // The real widget initializes its editors only after XML inflation.
            (LayoutInflater.from(themed).inflate(R.layout.sample_dialer_filter, null)
                    as DialerFilter)
                .apply {
                    id = viewId
                    val watcher =
                        object : TextWatcher {
                            override fun beforeTextChanged(
                                s: CharSequence?,
                                a: Int,
                                b: Int,
                                c: Int,
                            ) {}

                            override fun onTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}

                            override fun afterTextChanged(s: Editable?) {
                                state.text = filterText.toString()
                            }
                        }
                    // View listeners survive the native mode's primary/hint text swaps.
                    children.filterIsInstance<EditText>().forEach {
                        it.addTextChangedListener(watcher)
                    }
                }
        },
        update = { filter ->
            val target = state.text
            if (filter.filterText.toString() != target) {
                filter.clearText()
                filter.append(target)
                state.text = filter.filterText.toString()
            }
            (filter as ViewGroup).children.forEach { it.isEnabled = enabled }
            filter.isEnabled = enabled
        },
        modifier = modifier.testTag("sample-${component.name.lowercase()}"),
    )
}
