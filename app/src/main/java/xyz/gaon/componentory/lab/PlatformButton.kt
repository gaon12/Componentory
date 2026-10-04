package xyz.gaon.componentory.lab

import android.view.ContextThemeWrapper
import android.widget.Button
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import xyz.gaon.componentory.R

@Composable
fun PlatformButton(
    family: PlatformFamily,
    viewId: Int,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Construct the framework class directly so the shell never substitutes a library widget.
    AndroidView(
        factory = { context ->
            Button(ContextThemeWrapper(context, family.themeId)).apply {
                id = viewId
                setText(R.string.sample_button)
            }
        },
        update = { button ->
            button.isEnabled = enabled
            button.setOnClickListener { onClick() }
        },
        modifier = modifier,
    )
}
