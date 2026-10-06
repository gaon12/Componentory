package xyz.gaon.componentory.lab

import android.view.ContextThemeWrapper
import android.view.Gravity
import android.widget.TabHost
import android.widget.TextView
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.viewinterop.AndroidView
import xyz.gaon.componentory.R

// TabHost.setCurrentTab already fires its change listener, so updates only need
// to compare against the widget's own index.
@Composable
internal fun PlatformTabSample(
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
            @Suppress("DEPRECATION")
            TabHost(themed).apply {
                id = viewId
                // setup() builds the real TabWidget strip and content frame.
                setup()
                repeat(component.tabCount) { index ->
                    addTab(
                        newTabSpec("tab_$panel$index")
                            .setIndicator(themed.getString(R.string.tab_indicator, index + 1))
                            .setContent {
                                TextView(themed).apply {
                                    gravity = Gravity.CENTER
                                    text = themed.getString(R.string.tab_content, index + 1)
                                }
                            }
                    )
                }
            }
        },
        update = { view ->
            @Suppress("DEPRECATION") val host = view as TabHost
            host.setOnTabChangedListener(null)
            val target = state.value.coerceIn(0, component.tabCount - 1)
            if (host.currentTab != target) host.currentTab = target
            host.setOnTabChangedListener { state.value = host.currentTab }
            host.isEnabled = enabled
            host.tabWidget.isEnabled = enabled
        },
        modifier = modifier.testTag("native_$panel"),
    )
}
