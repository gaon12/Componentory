// Samples intentionally compare Material 2 and Material 3 under their own themes.
@file:Suppress("UsingMaterialAndMaterial3Libraries")

package xyz.gaon.componentory.lab.recreation

import android.graphics.drawable.GradientDrawable
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Button as AndroidButton
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.Button as Material2Button
import androidx.compose.material.MaterialTheme as Material2Theme
import androidx.compose.material.Text as Material2Text
import androidx.compose.material.lightColors
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalAccessibilityManager
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import kotlinx.coroutines.delay
import xyz.gaon.componentory.R
import xyz.gaon.componentory.lab.DesignFamily
import xyz.gaon.componentory.lab.LabComponent
import xyz.gaon.componentory.lab.Material3SampleTheme
import xyz.gaon.componentory.lab.PlatformFamily
import xyz.gaon.componentory.lab.ReadableAndroidView
import xyz.gaon.componentory.lab.SampleState

/** Source layouts shown inside the app, rather than the installed OS Toast renderer. */
@Composable
internal fun ToastSample(
    family: DesignFamily,
    panel: String,
    enabled: Boolean,
    state: SampleState,
    modifier: Modifier = Modifier,
) {
    if (family == DesignFamily.MATERIAL2)
        Material2Theme(colors = lightColors()) {
            ToastContent(family, panel, enabled, state, modifier)
        }
    else if (family.isMaterial3)
        Material3SampleTheme(family) { ToastContent(family, panel, enabled, state, modifier) }
    else ToastContent(family, panel, enabled, state, modifier)
}

@Composable
private fun ToastContent(
    family: DesignFamily,
    panel: String,
    enabled: Boolean,
    state: SampleState,
    modifier: Modifier,
) {
    var generation by remember { mutableIntStateOf(0) }
    var visible by remember { mutableStateOf(false) }
    val message = stringResource(R.string.toast_message)
    val action = stringResource(R.string.show_toast)
    val resources = ResourceToasts.forFamily(family)
    val surface = MaterialTheme.colorScheme.surface.toArgb()
    val foreground = MaterialTheme.colorScheme.onSurface.toArgb()
    val accessibility = LocalAccessibilityManager.current
    val timeout =
        accessibility?.calculateRecommendedTimeoutMillis(
            2000L,
            containsIcons = resources.hasIcon,
            containsText = true,
            containsControls = false,
        ) ?: 2000L
    LaunchedEffect(generation) {
        visible = generation > 0 && enabled
        if (visible) {
            delay(timeout)
            visible = false
        }
    }
    LaunchedEffect(enabled) { if (!enabled) visible = false }
    fun open() {
        if (enabled) {
            state.value++
            generation++
        }
    }
    Column(modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        ToastArtwork(
            resources,
            message,
            surface,
            foreground,
            Modifier.testTag("toast_preview_$panel"),
        )
        val platform = family.platform
        if (platform != null) {
            ReadableAndroidView(
                factory = { context ->
                    AndroidButton(
                            HistoricalControls.context(context, platform, LabComponent.BUTTON)
                        )
                        .apply {
                            id = if (panel == "LEFT") R.id.sample_left else R.id.sample_right
                            text = action
                            HistoricalControls.apply(this, platform, LabComponent.BUTTON)
                        }
                },
                update = { button ->
                    button.isEnabled = enabled
                    button.setOnClickListener { open() }
                },
                modifier = Modifier.testTag("native_$panel"),
            )
        } else if (family == DesignFamily.MATERIAL2)
            Material2Button(
                onClick = ::open,
                enabled = enabled,
                modifier = Modifier.testTag("library_$panel"),
            ) {
                Material2Text(action)
            }
        else
            Button(
                onClick = ::open,
                enabled = enabled,
                modifier = Modifier.testTag("library_$panel"),
            ) {
                Text(action)
            }
    }
    if (visible) {
        val density = LocalDensity.current
        val bottom =
            WindowInsets.safeDrawing.getBottom(density) + with(density) { 64.dp.roundToPx() }
        val edge = with(density) { 16.dp.roundToPx() }
        val position = remember(bottom, edge) { ToastPosition(bottom, edge) }
        Popup(
            popupPositionProvider = position,
            properties =
                PopupProperties(
                    // A toast must let touches reach the controls underneath its artwork.
                    flags =
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                    dismissOnBackPress = false,
                    dismissOnClickOutside = false,
                    excludeFromSystemGesture = false,
                ),
        ) {
            ToastArtwork(
                resources,
                message,
                surface,
                foreground,
                Modifier.testTag("toast_popup_$panel").semantics {
                    liveRegion = LiveRegionMode.Polite
                },
            )
        }
    }
}

private class ToastPosition(private val bottom: Int, private val edge: Int) :
    PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize,
    ): IntOffset {
        val maximumX = (windowSize.width - popupContentSize.width).coerceAtLeast(0)
        val maximumY = (windowSize.height - popupContentSize.height).coerceAtLeast(0)
        return IntOffset(
            maximumX / 2,
            (maximumY - bottom).coerceIn(edge.coerceAtMost(maximumY), maximumY),
        )
    }
}

@Composable
private fun ToastArtwork(
    resources: ResourceToasts.Release,
    message: String,
    surface: Int,
    foreground: Int,
    modifier: Modifier,
) {
    AndroidView(
        factory = { base ->
            val context = PlatformFamily.MATERIAL.createContext(base)
            // Supplying a detached parent preserves margins while inflation stays off the OS toast
            // path.
            val parent = FrameLayout(context)
            LayoutInflater.from(context).inflate(resources.layout, parent, false).apply {
                findViewById<ImageView>(R.id.aosp_toast_icon)
                    ?.setImageResource(R.drawable.ic_launcher_legacy)
                setTag(R.id.aosp_resource_revision, resources.release)
            }
        },
        update = { root ->
            val text =
                root.findViewById<TextView>(R.id.aosp_toast_text)
                    ?: requireNotNull(root.findViewById<TextView>(android.R.id.message))
            text.text = message
            if (resources.libraryPalette) {
                (root.background.mutate() as GradientDrawable).setColor(surface)
                text.setTextColor(foreground)
            }
            if (resources.lineHeightSp > 0) {
                val pixels =
                    TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_SP,
                        resources.lineHeightSp,
                        root.resources.displayMetrics,
                    )
                text.setLineSpacing(
                    (pixels - text.paint.fontMetricsInt.let { it.descent - it.ascent })
                        .coerceAtLeast(0f),
                    1f,
                )
            }
            root.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_YES
        },
        modifier = modifier.widthIn(max = 320.dp),
    )
}
