package xyz.gaon.componentory.lab

import android.view.ContextThemeWrapper
import android.view.Menu
import android.widget.Button
import android.widget.PopupMenu
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import xyz.gaon.componentory.R

@Composable
internal fun PlatformPopupMenuSample(
    family: PlatformFamily,
    viewId: Int,
    enabled: Boolean,
    state: SampleState,
    modifier: Modifier,
) {
    val launcher = remember { mutableStateOf<Button?>(null) }
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) launcher.value?.let(::releasePopupMenu)
        }
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
            launcher.value?.let(::releasePopupMenu)
        }
    }
    AndroidView(
        factory = { context ->
            Button(ContextThemeWrapper(context, family.themeId)).apply {
                id = viewId
                setText(R.string.open_menu)
                launcher.value = this
            }
        },
        update = { view ->
            view.isEnabled = enabled
            (view.tag as? PopupMenu)?.let { popup ->
                popup.menu.findItem(1).isEnabled = enabled
                popup.menu.findItem(2).isEnabled = enabled
                if (!enabled) releasePopupMenu(view)
            }
            view.setOnClickListener {
                if (view.tag is PopupMenu) return@setOnClickListener
                showPopupMenu(view, enabled, state)
            }
        },
        onReset = null,
        onRelease = { view ->
            releasePopupMenu(view)
            view.setOnClickListener(null)
            launcher.value = null
        },
        modifier =
            modifier.testTag(if (viewId == R.id.sample_left) "native_LEFT" else "native_RIGHT"),
    )
}

private fun showPopupMenu(anchor: Button, enabled: Boolean, state: SampleState) {
    val popup = PopupMenu(anchor.context, anchor)
    popup.menu.add(Menu.NONE, 1, Menu.NONE, anchor.context.getString(R.string.option_a)).isEnabled =
        enabled
    popup.menu.add(Menu.NONE, 2, Menu.NONE, anchor.context.getString(R.string.option_b)).isEnabled =
        enabled
    popup.menu
        .add(Menu.NONE, 3, Menu.NONE, anchor.context.getString(R.string.menu_disabled_choice))
        .isEnabled = false
    popup.setOnMenuItemClickListener { item ->
        state.value = item.itemId
        state.text = SampleMenuAction.SELECTED.name
        true
    }
    popup.setOnDismissListener { dismissed ->
        if (anchor.tag === dismissed) {
            anchor.tag = null
            if (state.text == SampleMenuAction.OPENED.name)
                state.text = SampleMenuAction.DISMISSED.name
        }
    }
    anchor.tag = popup
    state.text = SampleMenuAction.OPENED.name
    popup.show()
}

private fun releasePopupMenu(anchor: Button) {
    val popup = anchor.tag as? PopupMenu ?: return
    // Removing a preview or disabling it is not a user dismissal.
    popup.setOnDismissListener(null)
    popup.setOnMenuItemClickListener(null)
    popup.dismiss()
    anchor.tag = null
}
