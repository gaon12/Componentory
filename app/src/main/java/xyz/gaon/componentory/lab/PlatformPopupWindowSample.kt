package xyz.gaon.componentory.lab

import android.graphics.drawable.ColorDrawable
import android.util.TypedValue
import android.view.ContextThemeWrapper
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListPopupWindow
import android.widget.PopupWindow
import android.widget.TextView
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
internal fun PlatformPopupWindowSample(
    family: PlatformFamily,
    component: LabComponent,
    viewId: Int,
    enabled: Boolean,
    state: SampleState,
    modifier: Modifier,
) {
    val launcher = remember { mutableStateOf<Button?>(null) }
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) launcher.value?.let(::releasePopupWindow)
        }
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
            launcher.value?.let(::releasePopupWindow)
        }
    }
    AndroidView(
        factory = { context ->
            Button(ContextThemeWrapper(context, family.themeId)).apply {
                id = viewId
                setText(R.string.open_popup)
                launcher.value = this
            }
        },
        update = { view ->
            view.isEnabled = enabled
            if (!enabled) releasePopupWindow(view)
            view.setOnClickListener {
                if (view.tag != null) return@setOnClickListener
                when (component) {
                    LabComponent.POPUP_WINDOW -> showPopupWindow(view, state)
                    else -> showListPopupWindow(view, state)
                }
            }
        },
        onReset = null,
        onRelease = { view ->
            releasePopupWindow(view)
            view.setOnClickListener(null)
            launcher.value = null
        },
        modifier =
            modifier.testTag(if (viewId == R.id.sample_left) "native_LEFT" else "native_RIGHT"),
    )
}

private fun showPopupWindow(anchor: Button, state: SampleState) {
    val density = anchor.resources.displayMetrics.density
    val popup =
        PopupWindow(anchor.context).apply {
            contentView =
                TextView(anchor.context).apply {
                    val pad = (20 * density).toInt()
                    setPadding(pad, pad, pad, pad)
                    setText(R.string.popup_content)
                }
            // Outside-tap dismissal only works once the window has a background.
            val background = TypedValue()
            anchor.context.theme.resolveAttribute(android.R.attr.colorBackground, background, true)
            setBackgroundDrawable(ColorDrawable(background.data))
            isOutsideTouchable = true
            width = ViewGroup.LayoutParams.WRAP_CONTENT
            height = ViewGroup.LayoutParams.WRAP_CONTENT
        }
    popup.setOnDismissListener { if (anchor.tag === popup) anchor.tag = null }
    anchor.tag = popup
    popup.showAsDropDown(anchor)
    state.value += 1
}

private fun showListPopupWindow(anchor: Button, state: SampleState) {
    val popup =
        ListPopupWindow(anchor.context).apply {
            setAdapter(
                ArrayAdapter(
                    anchor.context,
                    android.R.layout.simple_list_item_1,
                    (1..4).map { anchor.context.getString(R.string.list_item, it) },
                )
            )
            anchorView = anchor
            isModal = true
            setOnItemClickListener { _, _, position, _ -> state.value = position + 1 }
        }
    popup.setOnDismissListener { if (anchor.tag === popup) anchor.tag = null }
    anchor.tag = popup
    popup.show()
}

private fun releasePopupWindow(anchor: Button) {
    when (val tag = anchor.tag) {
        is PopupWindow -> {
            // Removing a preview or disabling it is not a user dismissal.
            tag.setOnDismissListener(null)
            tag.dismiss()
        }
        is ListPopupWindow -> {
            tag.setOnDismissListener(null)
            tag.dismiss()
        }
    }
    anchor.tag = null
}
