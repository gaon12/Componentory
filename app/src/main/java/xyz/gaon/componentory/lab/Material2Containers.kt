// These samples use Material 2 overloads, separate from the app's Material 3 controls.
@file:Suppress("UsingMaterialAndMaterial3Libraries")

package xyz.gaon.componentory.lab

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import xyz.gaon.componentory.R

@OptIn(ExperimentalMaterialApi::class)
@Composable
internal fun Material2Containers(
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
                    Material2ContainerContent(component)
                }
            } else {
                Card(modifier = container) { Material2ContainerContent(component) }
            }
        LabComponent.SURFACE ->
            if (state.containerClickable) {
                Surface(onClick = { state.value++ }, enabled = enabled, modifier = container) {
                    Material2ContainerContent(component)
                }
            } else {
                Surface(modifier = container) { Material2ContainerContent(component) }
            }
        else -> error("Unsupported Material 2 container: $component")
    }
}

@Composable
private fun Material2ContainerContent(component: LabComponent) {
    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(stringResource(component.labelRes))
        Text(stringResource(R.string.sample_container_content))
    }
}
