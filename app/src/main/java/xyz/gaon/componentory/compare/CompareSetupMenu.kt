package xyz.gaon.componentory.compare

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import xyz.gaon.componentory.R

internal enum class CopySetupDirection {
    LEFT_TO_RIGHT,
    RIGHT_TO_LEFT,
}

@Composable
internal fun CompareSetupMenu(
    onPreview: (CopySetupDirection) -> SetupCopyResult,
    onCopy: (CopySetupDirection) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val left = stringResource(R.string.left_ui)
    val right = stringResource(R.string.right_ui)
    Box {
        TextButton(onClick = { expanded = true }, modifier = Modifier.testTag("copy_inputs")) {
            Text(stringResource(R.string.copy_inputs))
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.widthIn(max = 320.dp).testTag("copy_inputs_menu"),
        ) {
            CopySetupDirection.entries.forEach { direction ->
                val result = onPreview(direction)
                val from = if (direction == CopySetupDirection.LEFT_TO_RIGHT) left else right
                val to = if (direction == CopySetupDirection.LEFT_TO_RIGHT) right else left
                DropdownMenuItem(
                    text = {
                        Column {
                            Text(stringResource(R.string.copy_setup_direction, from, to))
                            result.reason?.let { reason ->
                                Text(
                                    stringResource(reason.labelRes),
                                    modifier = Modifier.testTag("copy_reason_${direction.name}"),
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            }
                        }
                    },
                    onClick = {
                        expanded = false
                        onCopy(direction)
                    },
                    enabled = result.state != null,
                    modifier = Modifier.testTag("copy_setup_${direction.name}"),
                )
            }
            Text(
                stringResource(R.string.copy_setup_note),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

private val SetupCopyReason.labelRes: Int
    get() =
        when (this) {
            SetupCopyReason.SOURCE_UNSUPPORTED -> R.string.copy_setup_source_unsupported
            SetupCopyReason.TARGET_UNSUPPORTED -> R.string.copy_setup_target_unsupported
            SetupCopyReason.NO_INPUTS -> R.string.copy_setup_no_inputs
            SetupCopyReason.ICON_UNAVAILABLE -> R.string.copy_setup_icon_incompatible
        }
