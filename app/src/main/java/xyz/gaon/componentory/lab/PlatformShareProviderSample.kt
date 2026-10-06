package xyz.gaon.componentory.lab

import android.content.Intent
import android.view.ContextThemeWrapper
import android.view.MenuItem
import android.view.View
import android.widget.ActionMenuView
import android.widget.ShareActionProvider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.viewinterop.AndroidView
import xyz.gaon.componentory.R

@Composable
internal fun PlatformShareProviderSample(
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
            createShareHost(themed, state).apply { id = viewId }
        },
        update = { view ->
            view.isEnabled = enabled
            (view.tag as? MenuItem)?.isEnabled = enabled
        },
        modifier = modifier.testTag("sample-${component.name.lowercase()}"),
    )
}

// ActionMenuView lazily builds its ActionMenuPresenter through getMenu(), so
// the item renders as a genuine action button. The ShareActionProvider sits
// on the item and opens the real share-target submenu; every target pick runs
// the provider's own listener, which is the reported state.
private fun createShareHost(themed: ContextThemeWrapper, state: SampleState): View {
    val host = ActionMenuView(themed)
    val provider =
        ShareActionProvider(themed).apply {
            setShareIntent(
                Intent(Intent.ACTION_SEND)
                    .setType("text/plain")
                    .putExtra(Intent.EXTRA_TEXT, themed.getString(R.string.share_text))
            )
            setOnShareTargetSelectedListener { _, _ ->
                state.value += 1
                false
            }
        }
    val item =
        host.menu.add(themed.getString(R.string.share_action)).apply {
            setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
            actionProvider = provider
        }
    host.tag = item
    return host
}
