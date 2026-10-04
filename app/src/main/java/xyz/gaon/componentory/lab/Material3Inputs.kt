package xyz.gaon.componentory.lab

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedSecureTextField
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SecureTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun Material3Inputs(
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
