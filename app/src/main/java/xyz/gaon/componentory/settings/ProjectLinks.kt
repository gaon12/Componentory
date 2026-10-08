package xyz.gaon.componentory.settings

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import xyz.gaon.componentory.R

@Composable
internal fun ProjectLinks() {
    ExternalLinkRows(
        listOf(
            ExternalLink(
                stringResource(R.string.project_repository),
                "https://github.com/gaon12/Componentory",
                "project_repository",
            ),
            ExternalLink(
                stringResource(R.string.project_feedback),
                "https://github.com/gaon12/Componentory/issues/new",
                "project_feedback",
            ),
        )
    )
}

internal data class ExternalLink(val label: String, val url: String, val tag: String)

@Composable
internal fun ExternalLinkRows(links: List<ExternalLink>) {
    val context = LocalContext.current
    var failed by remember { mutableStateOf(false) }
    val open: (String) -> Unit = { url ->
        try {
            context.startActivity(externalBrowserIntent(url))
            failed = false
        } catch (_: ActivityNotFoundException) {
            failed = true
        }
    }
    links.forEachIndexed { index, link ->
        ProjectLinkRow(link.label, link.url, link.tag, open)
        if (index < links.lastIndex) HorizontalDivider(Modifier.padding(horizontal = 18.dp))
    }
    if (failed)
        Text(
            stringResource(R.string.project_link_failed),
            Modifier.padding(18.dp).testTag("project_link_failed"),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error,
        )
}

@Composable
private fun ProjectLinkRow(label: String, url: String, tag: String, onOpen: (String) -> Unit) {
    Row(
        Modifier.fillMaxWidth()
            .heightIn(min = 64.dp)
            .clickable(role = Role.Button) { onOpen(url) }
            .testTag(tag)
            .padding(18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(label, Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
        Icon(
            Icons.AutoMirrored.Filled.OpenInNew,
            null,
            Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

internal fun externalBrowserIntent(url: String): Intent =
    Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
        addCategory(Intent.CATEGORY_BROWSABLE)
        // Resolve a browser even when a verified app owns the destination's links.
        selector = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_APP_BROWSER)
    }
