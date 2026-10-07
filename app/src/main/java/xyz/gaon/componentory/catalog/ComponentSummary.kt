package xyz.gaon.componentory.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Article
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.PlayCircleOutline
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.TouchApp
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.ViewQuilt
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import xyz.gaon.componentory.BuildConfig
import xyz.gaon.componentory.history.androidVersionLabel
import xyz.gaon.componentory.lab.ComponentCategory
import xyz.gaon.componentory.lab.LabComponent

internal fun componentVersionSummary(component: LabComponent): String =
    if (component.platformSource != null)
        androidVersionLabel(component.minimumApi).replace(" ·", "+ ·") + "+"
    else
        buildList {
                if (component.material2Function != null)
                    add("Material 2 ${BuildConfig.MATERIAL2_VERSION}")
                if (component.material3Function != null)
                    add("Material 3 ${BuildConfig.MATERIAL3_VERSION}")
            }
            .joinToString(" · ")

@Composable
internal fun ComponentSummary(
    component: LabComponent,
    modifier: Modifier = Modifier,
    providers: String? = null,
    trailing: @Composable () -> Unit,
) {
    Row(
        modifier.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            Modifier.size(44.dp)
                .background(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.shapes.small),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                when (component.category) {
                    ComponentCategory.ACTION -> Icons.Outlined.TouchApp
                    ComponentCategory.SELECTION -> Icons.Outlined.CheckCircle
                    ComponentCategory.INPUT -> Icons.Outlined.Edit
                    ComponentCategory.INDICATOR -> Icons.Outlined.Tune
                    ComponentCategory.PICKER -> Icons.Outlined.Schedule
                    ComponentCategory.FEEDBACK -> Icons.Outlined.ChatBubbleOutline
                    ComponentCategory.CONTENT -> Icons.Outlined.Article
                    ComponentCategory.NAVIGATION -> Icons.Outlined.Menu
                    ComponentCategory.LAYOUT -> Icons.Outlined.ViewQuilt
                    ComponentCategory.MEDIA -> Icons.Outlined.PlayCircleOutline
                    ComponentCategory.LEGACY -> Icons.Outlined.History
                },
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp),
            )
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(stringResource(component.labelRes), style = MaterialTheme.typography.titleMedium)
            Text(
                componentVersionSummary(component),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.testTag("versions_${component.name}"),
            )
            Text(
                stringResource(component.descriptionRes),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (providers != null)
                Text(
                    providers,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.testTag("providers_${component.name}"),
                )
        }
        trailing()
    }
}
