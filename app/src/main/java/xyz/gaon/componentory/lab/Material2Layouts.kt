// The lab keeps samples from different Material libraries separate.
@file:Suppress("UsingMaterialAndMaterial3Libraries")

package xyz.gaon.componentory.lab

import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
internal fun Material2Layouts(component: LabComponent, modifier: Modifier) {
    when (component) {
        LabComponent.HORIZONTAL_DIVIDER -> Divider(modifier = modifier)
        else -> error("Unsupported Material 2 layout: $component")
    }
}
