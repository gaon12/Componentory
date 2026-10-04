package xyz.gaon.componentory.lab

import androidx.compose.foundation.layout.height
import androidx.compose.material3.Divider as LegacyDivider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// Keep the deprecated function available as its own source sample.
@Suppress("DEPRECATION")
@Composable
internal fun Material3Layouts(component: LabComponent, modifier: Modifier) {
    when (component) {
        LabComponent.HORIZONTAL_DIVIDER -> HorizontalDivider(modifier = modifier)
        LabComponent.VERTICAL_DIVIDER -> VerticalDivider(modifier = modifier.height(64.dp))
        LabComponent.LEGACY_DIVIDER -> LegacyDivider(modifier = modifier)
        else -> error("Unsupported Material 3 layout: $component")
    }
}
