// Samples use their actual library so the two design systems can be compared.
@file:Suppress("UsingMaterialAndMaterial3Libraries")

package xyz.gaon.componentory.lab

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Badge
import androidx.compose.material.BadgedBox
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import xyz.gaon.componentory.icons.LocalSampleIcon

@Composable
internal fun Material2Indicators(
    component: LabComponent,
    panel: String,
    modifier: Modifier,
    state: SampleState,
) {
    when (component) {
        LabComponent.PROGRESS ->
            LinearProgressIndicator(
                progress = state.value / 100f,
                modifier = modifier.fillMaxWidth(),
            )
        LabComponent.CIRCULAR_PROGRESS ->
            CircularProgressIndicator(progress = state.value / 100f, modifier = modifier)
        LabComponent.INDETERMINATE_LINEAR_PROGRESS ->
            LinearProgressIndicator(modifier = modifier.fillMaxWidth())
        LabComponent.INDETERMINATE_CIRCULAR_PROGRESS ->
            CircularProgressIndicator(modifier = modifier)
        LabComponent.BADGE ->
            Badge(modifier = modifier) {
                Text(state.value.toString(), Modifier.testTag("badge_count_$panel"))
            }
        LabComponent.DOT_BADGE -> Badge(modifier = modifier)
        LabComponent.BADGED_BOX ->
            BadgedBox(
                badge = {
                    Badge { Text(state.value.toString(), Modifier.testTag("badge_count_$panel")) }
                },
                modifier = modifier,
            ) {
                val icon = requireNotNull(LocalSampleIcon.current)
                Icon(
                    icon.vector(),
                    contentDescription = icon.name,
                    modifier = Modifier.testTag("badge_icon_$panel"),
                )
            }
        else -> error("Unsupported Material 2 indicator: $component")
    }
}
