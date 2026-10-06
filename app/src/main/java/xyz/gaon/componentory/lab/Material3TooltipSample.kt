package xyz.gaon.componentory.lab

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Label
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.RichTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import xyz.gaon.componentory.R

// Tooltips are transient: TooltipBox shows them on long press or hover, so the
// panel keeps a real anchored tooltip but copies nothing.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun Material3TooltipSample(
    component: LabComponent,
    panel: String,
    modifier: Modifier,
    enabled: Boolean,
) {
    Column(modifier) {
        when (component) {
            LabComponent.PLAIN_TOOLTIP ->
                TooltipBox(
                    positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                    tooltip = {
                        PlainTooltip(modifier = Modifier.testTag("library_${panel}_tooltip")) {
                            Text(stringResource(R.string.tooltip_text))
                        }
                    },
                    state = rememberTooltipState(),
                    modifier = Modifier.testTag("library_${panel}_anchor"),
                ) {
                    TextButton(onClick = {}, enabled = enabled) {
                        Text(stringResource(R.string.tooltip_hint))
                    }
                }
            LabComponent.RICH_TOOLTIP ->
                TooltipBox(
                    positionProvider = TooltipDefaults.rememberRichTooltipPositionProvider(),
                    tooltip = {
                        RichTooltip(
                            title = { Text(stringResource(R.string.tooltip_title)) },
                            action = {
                                TextButton(onClick = {}) {
                                    Text(stringResource(R.string.tooltip_action))
                                }
                            },
                            modifier = Modifier.testTag("library_${panel}_tooltip"),
                        ) {
                            Text(stringResource(R.string.tooltip_text))
                        }
                    },
                    state = rememberTooltipState(),
                    modifier = Modifier.testTag("library_${panel}_anchor"),
                ) {
                    TextButton(onClick = {}, enabled = enabled) {
                        Text(stringResource(R.string.tooltip_hint))
                    }
                }
            LabComponent.LABEL -> {
                // Label shows its tooltip while the shared interaction source
                // reports a press or hover on the wrapped content.
                val interactions = remember { MutableInteractionSource() }
                Label(
                    label = {
                        PlainTooltip(modifier = Modifier.testTag("library_${panel}_tooltip")) {
                            Text(stringResource(R.string.tooltip_text))
                        }
                    },
                    interactionSource = interactions,
                    modifier = Modifier.testTag("library_${panel}_anchor"),
                ) {
                    TextButton(onClick = {}, enabled = enabled, interactionSource = interactions) {
                        Text(stringResource(R.string.tooltip_hint))
                    }
                }
            }
            else -> error("Unsupported components must be handled by SamplePanel.")
        }
    }
}
