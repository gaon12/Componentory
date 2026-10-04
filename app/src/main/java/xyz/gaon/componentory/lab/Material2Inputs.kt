@file:Suppress("UsingMaterialAndMaterial3Libraries")

package xyz.gaon.componentory.lab

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.OutlinedSecureTextField
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.SecureTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterialApi::class)
@Composable
internal fun Material2Inputs(
    component: LabComponent,
    modifier: Modifier,
    enabled: Boolean,
    state: SampleState,
) {
    when (component) {
        LabComponent.OUTLINED_TEXT_FIELD ->
            OutlinedTextField(
                value = state.text,
                onValueChange = { state.text = it },
                enabled = enabled,
                singleLine = true,
                label = { Text("Type something") },
                modifier = modifier,
            )
        LabComponent.SECURE_TEXT_FIELD ->
            SecureTextField(
                state = rememberSecureSampleState(state),
                enabled = enabled,
                label = { Text("Sample password") },
                modifier = modifier,
            )
        LabComponent.OUTLINED_SECURE_TEXT_FIELD ->
            OutlinedSecureTextField(
                state = rememberSecureSampleState(state),
                enabled = enabled,
                label = { Text("Sample password") },
                modifier = modifier,
            )
        else -> error("Unsupported components must be handled by SamplePanel.")
    }
}
