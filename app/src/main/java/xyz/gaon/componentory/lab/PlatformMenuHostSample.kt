package xyz.gaon.componentory.lab

import android.util.TypedValue
import android.view.ContextThemeWrapper
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ActionMenuView
import android.widget.Toolbar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.viewinterop.AndroidView
import xyz.gaon.componentory.R

@Composable
internal fun PlatformMenuHostSample(
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
            createMenuHost(themed, component).apply { id = viewId }
        },
        update = { view ->
            view.isEnabled = enabled
            if (view is Toolbar) {
                view.setOnMenuItemClickListener { item ->
                    state.value = item.itemId
                    true
                }
                view.menu.findItem(1).isEnabled = enabled
                view.menu.findItem(2).isEnabled = enabled
                view.setNavigationOnClickListener { state.value = 3 }
            } else {
                val menuView = view as ActionMenuView
                menuView.setOnMenuItemClickListener { item ->
                    state.value = item.itemId
                    true
                }
                menuView.menu.findItem(1).isEnabled = enabled
                menuView.menu.findItem(2).isEnabled = enabled
            }
        },
        onReset = null,
        onRelease = { view ->
            if (view is Toolbar) {
                view.setOnMenuItemClickListener(null)
                view.setNavigationOnClickListener(null)
            } else {
                (view as ActionMenuView).setOnMenuItemClickListener(null)
            }
        },
        modifier =
            modifier.testTag(if (viewId == R.id.sample_left) "native_LEFT" else "native_RIGHT"),
    )
}

private fun createMenuHost(themed: ContextThemeWrapper, component: LabComponent): View =
    when (component) {
        LabComponent.TOOLBAR ->
            Toolbar(themed).apply {
                setTitle(R.string.toolbar_title)
                setSubtitle(R.string.toolbar_subtitle)
                // The real theme's own up indicator, not a bundled drawable.
                val icon = TypedValue()
                if (themed.theme.resolveAttribute(android.R.attr.homeAsUpIndicator, icon, true)) {
                    setNavigationIcon(icon.resourceId)
                }
                navigationContentDescription = themed.getString(R.string.toolbar_navigate)
                menu
                    .add(Menu.NONE, 1, Menu.NONE, R.string.option_a)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
                menu
                    .add(Menu.NONE, 2, Menu.NONE, R.string.option_b)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
            }
        else ->
            // getMenu() lazily builds the real ActionMenuPresenter, so items
            // appear as genuine action buttons instead of a drawn copy.
            ActionMenuView(themed).apply {
                menu.add(Menu.NONE, 1, Menu.NONE, R.string.option_a)
                menu.add(Menu.NONE, 2, Menu.NONE, R.string.option_b)
            }
    }
