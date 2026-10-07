package xyz.gaon.componentory.lab

import android.view.ContextThemeWrapper
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.QuickContactBadge
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.viewinterop.AndroidView
import xyz.gaon.componentory.R

@Composable
internal fun PlatformContentSurfaceSample(
    family: PlatformFamily,
    component: LabComponent,
    viewId: Int,
    enabled: Boolean,
    modifier: Modifier,
) {
    AndroidView(
        factory = { context ->
            val themed = family.createContext(context)
            createContentSurface(themed, component).apply { id = viewId }
        },
        update = { view -> view.isEnabled = enabled },
        modifier = modifier.testTag("sample-${component.name.lowercase()}"),
    )
}

// WebView renders real localized HTML inside the themed context; the default
// WebViewClient keeps link navigation inside the widget. QuickContactBadge is
// assigned a fixed address so taps open the real framework contact overlay.
private fun createContentSurface(themed: ContextThemeWrapper, component: LabComponent): View =
    when (component) {
        LabComponent.WEB_VIEW ->
            WebView(themed).apply {
                webViewClient = WebViewClient()
                minimumHeight = (96 * resources.displayMetrics.density).toInt()
                loadDataWithBaseURL(null, pageHtml(themed), "text/html", "utf-8", null)
            }
        else ->
            QuickContactBadge(themed).apply {
                assignContactFromEmail("componentory@example.com", true)
                setImageToDefault()
            }
    }

private fun pageHtml(themed: ContextThemeWrapper): String =
    "<html><body>" +
        "<h3>${themed.getString(R.string.component_web_view)}</h3>" +
        "<p>${themed.getString(R.string.webview_body)}</p>" +
        "<a href=\"https://developer.android.com\">" +
        themed.getString(R.string.webview_link) +
        "</a></body></html>"
