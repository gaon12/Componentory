package xyz.gaon.componentory.lab

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import xyz.gaon.componentory.R

@Composable
internal fun Material3Containers(
    component: LabComponent,
    modifier: Modifier,
    enabled: Boolean,
    state: SampleState,
) {
    val container = modifier.fillMaxWidth()
    when (component) {
        LabComponent.CARD ->
            if (state.containerClickable) {
                Card(onClick = { state.value++ }, enabled = enabled, modifier = container) {
                    Material3ContainerContent(component)
                }
            } else {
                Card(modifier = container) { Material3ContainerContent(component) }
            }
        LabComponent.ELEVATED_CARD ->
            if (state.containerClickable) {
                ElevatedCard(onClick = { state.value++ }, enabled = enabled, modifier = container) {
                    Material3ContainerContent(component)
                }
            } else {
                ElevatedCard(modifier = container) { Material3ContainerContent(component) }
            }
        LabComponent.OUTLINED_CARD ->
            if (state.containerClickable) {
                OutlinedCard(onClick = { state.value++ }, enabled = enabled, modifier = container) {
                    Material3ContainerContent(component)
                }
            } else {
                OutlinedCard(modifier = container) { Material3ContainerContent(component) }
            }
        LabComponent.SURFACE ->
            if (state.containerClickable) {
                Surface(onClick = { state.value++ }, enabled = enabled, modifier = container) {
                    Material3ContainerContent(component)
                }
            } else {
                Surface(modifier = container) { Material3ContainerContent(component) }
            }
        else -> error("Unsupported Material 3 container: $component")
    }
}

@Composable
private fun Material3ContainerContent(component: LabComponent) {
    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(stringResource(component.labelRes))
        Text(stringResource(R.string.sample_container_content))
    }
}
