package xyz.gaon.componentory.lab

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import xyz.gaon.componentory.icons.LocalSampleIcon

@Composable
internal fun Material3Indicators(
    component: LabComponent,
    panel: String,
    modifier: Modifier,
    state: SampleState,
) {
    when (component) {
        LabComponent.PROGRESS ->
            LinearProgressIndicator(
                progress = { state.value / 100f },
                modifier = modifier.fillMaxWidth(),
            )
        LabComponent.CIRCULAR_PROGRESS ->
            CircularProgressIndicator(progress = { state.value / 100f }, modifier = modifier)
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
        else -> error("Unsupported Material 3 indicator: $component")
    }
}
