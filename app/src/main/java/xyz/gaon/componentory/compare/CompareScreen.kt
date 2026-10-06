package xyz.gaon.componentory.compare

import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import xyz.gaon.componentory.R
import xyz.gaon.componentory.lab.DesignFamily
import xyz.gaon.componentory.lab.LabComponent
import xyz.gaon.componentory.lab.SamplePanel
import xyz.gaon.componentory.lab.SampleState
import xyz.gaon.componentory.lab.rememberSampleStateSlot

@Composable
fun CompareScreen(
    component: LabComponent,
    onComponentChange: (LabComponent) -> Unit,
    left: DesignFamily,
    onLeftChange: (DesignFamily) -> Unit,
    right: DesignFamily,
    onRightChange: (DesignFamily) -> Unit,
    initialEntry: ComparisonEntry? = null,
) {
    val context = LocalContext.current
    var enabled by rememberSaveable { mutableStateOf(initialEntry?.enabled ?: true) }
    var reset by rememberSaveable { mutableIntStateOf(0) }
    var useInitialSetup by rememberSaveable { mutableStateOf(true) }
    var copiedDirection by rememberSaveable { mutableStateOf<CopySetupDirection?>(null) }
    var iconSkipped by rememberSaveable { mutableStateOf(false) }
    // Layout changes must move the same experiment, not create new panel values.
    val seedState: (SampleSetup?, DesignFamily) -> SampleState = { setup, family ->
        if (useInitialSetup && setup?.component == component && setup.sourceFamily == family) {
            copySampleSetup(context, setup, family, SampleState(component.initialValue)).state
                ?: SampleState(component.initialValue)
        } else SampleState(component.initialValue)
    }
    var leftState by
        rememberSampleStateSlot("LEFT", left, component, reset) {
            seedState(initialEntry?.left, left)
        }
    var rightState by
        rememberSampleStateSlot("RIGHT", right, component, reset) {
            seedState(initialEntry?.right, right)
        }
    // The entry seeds this session once. Restored panel state owns all later changes.
    SideEffect { useInitialSetup = false }
    val clearEntryAndResult = {
        useInitialSetup = false
        copiedDirection = null
        iconSkipped = false
    }
    val changeLeft: (DesignFamily) -> Unit = {
        clearEntryAndResult()
        onLeftChange(it)
    }
    val changeRight: (DesignFamily) -> Unit = {
        clearEntryAndResult()
        onRightChange(it)
    }
    val previewCopy: (CopySetupDirection) -> SetupCopyResult = { direction ->
        val fromLeft = direction == CopySetupDirection.LEFT_TO_RIGHT
        val setup =
            captureSampleSetup(
                context,
                component,
                if (fromLeft) left else right,
                if (fromLeft) leftState else rightState,
            )
        copySampleSetup(
            context,
            setup,
            if (fromLeft) right else left,
            if (fromLeft) rightState else leftState,
        )
    }
    val enabledLabel = stringResource(R.string.enabled)
    Column(
        Modifier.widthIn(max = 1100.dp)
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .testTag("compare_screen")
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(stringResource(R.string.nav_compare), style = MaterialTheme.typography.headlineMedium)
        Text(
            stringResource(R.string.compare_intro, Build.VERSION.RELEASE),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        ComponentPicker(component) {
            clearEntryAndResult()
            onComponentChange(it)
        }
        FlowRow(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            itemVerticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(
                    checked = enabled,
                    onCheckedChange = { enabled = it },
                    modifier =
                        Modifier.testTag("enabled").semantics { contentDescription = enabledLabel },
                )
                Text(
                    enabledLabel,
                    modifier = Modifier.padding(horizontal = 12.dp).clearAndSetSemantics {},
                )
            }
            CompareSetupMenu(
                onPreview = previewCopy,
                onCopy = { direction ->
                    val result = previewCopy(direction)
                    result.state?.let { newState ->
                        useInitialSetup = false
                        if (direction == CopySetupDirection.LEFT_TO_RIGHT) rightState = newState
                        else leftState = newState
                        copiedDirection = direction
                        iconSkipped = result.iconSkipped
                    }
                },
            )
            TextButton(
                onClick = {
                    clearEntryAndResult()
                    reset++
                },
                modifier = Modifier.testTag("reset"),
            ) {
                Text(stringResource(R.string.reset))
            }
        }
        copiedDirection?.let { direction ->
            Text(
                stringResource(
                    R.string.copy_setup_done,
                    stringResource(
                        if (direction == CopySetupDirection.LEFT_TO_RIGHT) R.string.right_ui
                        else R.string.left_ui
                    ),
                ),
                modifier = Modifier.testTag("copy_setup_result"),
                style = MaterialTheme.typography.bodySmall,
            )
            if (iconSkipped) {
                Text(
                    stringResource(R.string.copy_setup_icon_skipped),
                    modifier = Modifier.testTag("copy_setup_icon_skipped"),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            if (maxWidth >= 600.dp) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    SamplePanel(
                        "LEFT",
                        left,
                        changeLeft,
                        component,
                        enabled,
                        reset,
                        leftState,
                        Modifier.weight(1f),
                        title = stringResource(R.string.left_ui),
                    )
                    SamplePanel(
                        "RIGHT",
                        right,
                        changeRight,
                        component,
                        enabled,
                        reset,
                        rightState,
                        Modifier.weight(1f),
                        title = stringResource(R.string.right_ui),
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    SamplePanel(
                        "LEFT",
                        left,
                        changeLeft,
                        component,
                        enabled,
                        reset,
                        leftState,
                        title = stringResource(R.string.left_ui),
                    )
                    SamplePanel(
                        "RIGHT",
                        right,
                        changeRight,
                        component,
                        enabled,
                        reset,
                        rightState,
                        title = stringResource(R.string.right_ui),
                    )
                }
            }
        }
    }
}
