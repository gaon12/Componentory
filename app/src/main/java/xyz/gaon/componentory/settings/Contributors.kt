package xyz.gaon.componentory.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import xyz.gaon.componentory.R

// Keep the verified snapshot offline; browsing settings must not require a network request.
internal val projectContributors = listOf("gaon12")
internal const val contributorsVerifiedOn = "2026-10-08"

@Composable
internal fun Contributors() {
    Column(Modifier.testTag("contributors_list")) {
        Text(
            stringResource(R.string.contributors_note),
            Modifier.padding(18.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        ExternalLinkRows(
            projectContributors.map { login ->
                ExternalLink(login, "https://github.com/$login", "contributor_$login")
            }
        )
        HorizontalDivider(Modifier.padding(horizontal = 18.dp))
        ExternalLinkRows(
            listOf(
                ExternalLink(
                    stringResource(R.string.contributors_on_github),
                    "https://github.com/gaon12/Componentory/graphs/contributors",
                    "contributors_on_github",
                )
            )
        )
        HorizontalDivider(Modifier.padding(horizontal = 18.dp))
        Text(
            stringResource(R.string.upstream_contributors),
            Modifier.padding(18.dp),
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            stringResource(R.string.upstream_contributors_note),
            Modifier.padding(horizontal = 18.dp),
            style = MaterialTheme.typography.bodyMedium,
        )
        ExternalLinkRows(
            listOf(
                ExternalLink(
                    "Hu Shenghao · Android Easter Eggs",
                    "https://github.com/hushenghao/AndroidEasterEggs",
                    "upstream_easter_eggs",
                ),
                ExternalLink(
                    "Android Open Source Project",
                    "https://android.googlesource.com/platform/frameworks/base/",
                    "upstream_aosp",
                ),
            )
        )
        Text(
            stringResource(R.string.contributors_verified, contributorsVerifiedOn),
            Modifier.padding(18.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
