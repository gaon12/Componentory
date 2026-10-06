package xyz.gaon.componentory.lab

import android.content.Context
import android.util.TypedValue
import android.view.ContextThemeWrapper
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.ZoomButton
import android.widget.ZoomButtonsController
import android.widget.ZoomControls
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.viewinterop.AndroidView
import xyz.gaon.componentory.R

private const val ZOOM_TARGET_HEIGHT_DP = 140

// Carries the controller on the host view's tag so updates can reach it.
private class ZoomSync(var controller: ZoomButtonsController? = null)

@Composable
internal fun PlatformZoomSample(
    family: PlatformFamily,
    component: LabComponent,
    viewId: Int,
    enabled: Boolean,
    state: SampleState,
    modifier: Modifier,
) {
    val panel = if (viewId == R.id.sample_left) "LEFT" else "RIGHT"
    AndroidView(
        factory = { context ->
            val themed = ContextThemeWrapper(context, family.themeId)
            createZoom(themed, component, state).apply { id = viewId }
        },
        update = { view ->
            val level = state.value.coerceIn(0, component.zoomLevelMax)
            when {
                view is ZoomControls -> {
                    view.setIsZoomInEnabled(enabled && level < component.zoomLevelMax)
                    view.setIsZoomOutEnabled(enabled && level > 0)
                }
                view is ZoomButton -> view.isEnabled = enabled && level < component.zoomLevelMax
                else ->
                    (view.tag as? ZoomSync)?.controller?.let { controller ->
                        view.isEnabled = enabled
                        controller.setZoomInEnabled(enabled && level < component.zoomLevelMax)
                        controller.setZoomOutEnabled(enabled && level > 0)
                        // The controller attaches its controls to the owner's parent,
                        // which only exists once the host is laid out.
                        if ((view as ViewGroup).childCount < 2)
                            view.post { controller.setVisible(true) }
                    }
            }
        },
        modifier = modifier.testTag("native_$panel"),
    )
}

@Suppress("DEPRECATION")
private fun createZoom(
    themed: ContextThemeWrapper,
    component: LabComponent,
    state: SampleState,
): View =
    when (component) {
        LabComponent.ZOOM_CONTROLS ->
            ZoomControls(themed).apply {
                tag = ZoomSync()
                setOnZoomInClickListener { state.value = (state.value + 1).coerceAtMost(10) }
                setOnZoomOutClickListener { state.value = (state.value - 1).coerceAtLeast(0) }
            }
        LabComponent.ZOOM_BUTTON ->
            ZoomButton(themed).apply {
                tag = ZoomSync()
                // The framework layout supplies the icon as a background, not an image.
                val zoomIn = ZoomControls(themed).getChildAt(1) as ZoomButton
                background = zoomIn.background
                contentDescription = zoomIn.contentDescription
                setOnClickListener { state.value = (state.value + 1).coerceAtMost(10) }
            }
        else ->
            FrameLayout(themed).apply {
                layoutParams =
                    ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        themed.dpToPx(ZOOM_TARGET_HEIGHT_DP),
                    )
                val owner =
                    TextView(themed).apply {
                        text = themed.getString(R.string.zoom_target)
                        gravity = Gravity.CENTER
                        layoutParams =
                            FrameLayout.LayoutParams(
                                FrameLayout.LayoutParams.MATCH_PARENT,
                                FrameLayout.LayoutParams.MATCH_PARENT,
                            )
                    }
                addView(owner)
                val controller =
                    ZoomButtonsController(owner).apply {
                        setAutoDismissed(false)
                        setOnZoomListener(
                            object : ZoomButtonsController.OnZoomListener {
                                override fun onVisibilityChanged(visible: Boolean) = Unit

                                override fun onZoom(zoomIn: Boolean) {
                                    state.value =
                                        (state.value + if (zoomIn) 1 else -1).coerceIn(0, 10)
                                }
                            }
                        )
                    }
                tag = ZoomSync(controller = controller)
            }
    }

private fun Context.dpToPx(dp: Int): Int =
    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), resources.displayMetrics)
        .toInt()
