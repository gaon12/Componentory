package xyz.gaon.componentory.lab

import android.view.ContextThemeWrapper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.FrameLayout
import android.widget.Gallery
import android.widget.SlidingDrawer
import android.widget.TextView
import android.widget.TwoLineListItem
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import xyz.gaon.componentory.R

@Suppress("DEPRECATION")
@Composable
internal fun PlatformLegacyContainerSample(
    family: PlatformFamily,
    component: LabComponent,
    viewId: Int,
    enabled: Boolean,
    state: SampleState,
    modifier: Modifier,
) {
    val panel = if (viewId == R.id.sample_left) "LEFT" else "RIGHT"
    // SlidingDrawer rejects an unspecified height inside the scrolling workspace.
    val sampleModifier =
        if (component == LabComponent.SLIDING_DRAWER)
            modifier.height(320.dp * LocalDensity.current.fontScale)
        else modifier
    ReadableAndroidView(
        factory = { context ->
            val themed = family.createContext(context)
            createLegacyContainer(themed, component, viewId)
        },
        update = { view ->
            when (component) {
                LabComponent.GALLERY -> {
                    val gallery = view as Gallery
                    gallery.onItemSelectedListener = null
                    val target = state.value.coerceIn(0, component.galleryItemCount - 1)
                    if (gallery.selectedItemPosition != target) {
                        // No animation on restore; taps still move with the
                        // widget's own fling behavior.
                        gallery.setSelection(target, false)
                    }
                    gallery.onItemSelectedListener =
                        object : AdapterView.OnItemSelectedListener {
                            override fun onItemSelected(
                                parent: AdapterView<*>,
                                itemView: View?,
                                position: Int,
                                id: Long,
                            ) {
                                state.value = position
                            }

                            override fun onNothingSelected(parent: AdapterView<*>) = Unit
                        }
                    gallery.isEnabled = enabled
                }
                LabComponent.SLIDING_DRAWER -> {
                    val drawer = view as SlidingDrawer
                    drawer.setOnDrawerOpenListener(null)
                    drawer.setOnDrawerCloseListener(null)
                    if (state.value == 1 && !drawer.isOpened) drawer.open()
                    if (state.value == 0 && drawer.isOpened) drawer.close()
                    drawer.setOnDrawerOpenListener { state.value = 1 }
                    drawer.setOnDrawerCloseListener { state.value = 0 }
                    // lock/unlock is the real API for disabling the handle.
                    if (enabled) drawer.unlock() else drawer.lock()
                }
                else -> Unit
            }
        },
        modifier = sampleModifier.testTag("native_$panel"),
    )
}

@Suppress("DEPRECATION")
private fun createLegacyContainer(
    themed: ContextThemeWrapper,
    component: LabComponent,
    viewId: Int,
): View =
    when (component) {
        LabComponent.GALLERY ->
            Gallery(themed).apply {
                id = viewId
                adapter =
                    ArrayAdapter(
                        themed,
                        android.R.layout.simple_list_item_1,
                        (1..component.galleryItemCount).map {
                            themed.getString(R.string.switcher_page, it)
                        },
                    )
            }
        LabComponent.SLIDING_DRAWER ->
            // The constructor throws without handle/content attrs, so the
            // widget is inflated from the app layout that declares them.
            (LayoutInflater.from(themed).inflate(R.layout.sample_sliding_drawer, null)
                    as SlidingDrawer)
                .apply {
                    id = viewId
                    findViewById<Button>(R.id.sliding_handle).setText(R.string.drawer_handle)
                    (findViewById<FrameLayout>(R.id.sliding_content)).addView(
                        TextView(themed).apply {
                            gravity = Gravity.CENTER
                            setText(R.string.drawer_content)
                        }
                    )
                }
        else ->
            (LayoutInflater.from(themed).inflate(R.layout.sample_two_line_list_item, null)
                    as TwoLineListItem)
                .apply {
                    id = viewId
                    findViewById<TextView>(android.R.id.text1).setText(R.string.two_line_primary)
                    findViewById<TextView>(android.R.id.text2).setText(R.string.two_line_secondary)
                }
    }
