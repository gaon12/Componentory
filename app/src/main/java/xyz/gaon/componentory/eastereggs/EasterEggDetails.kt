package xyz.gaon.componentory.eastereggs

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.dede.basic.getActivity
import xyz.gaon.componentory.R
import xyz.gaon.componentory.settings.ExternalLink
import xyz.gaon.componentory.settings.ExternalLinkRows
import xyz.gaon.componentory.settings.SettingsListRow

@Composable
internal fun EasterEggDetails(release: EasterEggRelease, onClose: () -> Unit) {
    val context = LocalContext.current
    var feedback by rememberSaveable(release.id) { mutableStateOf<Int?>(null) }
    val notifications =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
            feedback =
                if (it) R.string.egg_notifications_allowed else R.string.egg_notifications_denied
        }
    Dialog(onDismissRequest = onClose) {
        Surface(
            shape = MaterialTheme.shapes.large,
            modifier = Modifier.fillMaxWidth().heightIn(max = 700.dp),
        ) {
            Column(
                Modifier.padding(24.dp).testTag("egg_details_${release.id}"),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    "${release.title} · ${release.family.nickname}",
                    style = MaterialTheme.typography.titleLarge,
                )
                Column(
                    Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        stringResource(R.string.egg_port_note),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(
                        stringResource(
                            R.string.egg_shared_versions,
                            easterEggReleases
                                .filter { it.family == release.family }
                                .joinToString { it.title },
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(
                        stringResource(eggHelpResource(release.family.module)),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    SettingsListRow(
                        stringResource(R.string.egg_open_logo),
                        tag = "egg_logo_${release.id}",
                        onClick = { feedback = launchEgg(context, release.logo) },
                    )
                    release.family.stages.forEach { stage ->
                        if (Build.VERSION.SDK_INT >= stage.minimumApi) {
                            SettingsListRow(
                                stringResource(eggStageTitle(stage)),
                                tag = "egg_stage_${stage.className}",
                                onClick = { feedback = launchEgg(context, stage) },
                            )
                        } else {
                            SettingsListRow(
                                stringResource(eggStageTitle(stage)),
                                value = stringResource(R.string.egg_requires_api, stage.minimumApi),
                            )
                        }
                    }
                    if (release.family.integrations.isNotEmpty()) {
                        HorizontalDivider()
                        Text(
                            stringResource(R.string.egg_integrations_note),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        release.family.integrations.forEach { integration ->
                            EggIntegrationRows(integration) { feedback = it }
                        }
                    }
                    if (
                        release.family.module in
                            listOf("Nougat", "R", "S", "Tiramisu", "Baklava", "CinnamonBun")
                    ) {
                        Text(
                            stringResource(R.string.egg_notifications_note),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        if (Build.VERSION.SDK_INT >= 33) {
                            SettingsListRow(
                                stringResource(R.string.egg_notifications),
                                onClick = {
                                    if (
                                        ContextCompat.checkSelfPermission(
                                            context,
                                            Manifest.permission.POST_NOTIFICATIONS,
                                        ) == PackageManager.PERMISSION_GRANTED
                                    ) {
                                        feedback = R.string.egg_notifications_allowed
                                    } else
                                        notifications.launch(Manifest.permission.POST_NOTIFICATIONS)
                                },
                            )
                        }
                    }
                    Text(
                        stringResource(
                            R.string.egg_environment,
                            Build.VERSION.RELEASE,
                            Build.VERSION.SDK_INT,
                        ),
                        style = MaterialTheme.typography.bodySmall,
                    )
                    ExternalLinkRows(
                        listOf(
                            ExternalLink(
                                stringResource(R.string.egg_source),
                                "$eggSourceRepository/tree/$eggSourceRevision/eggs/${release.family.module}",
                                "egg_source",
                            )
                        )
                    )
                    Text(
                        "Apache 2.0 · Hu Shenghao · AOSP\n$eggSourceRevision",
                        style = MaterialTheme.typography.bodySmall,
                    )
                    feedback?.let {
                        Text(
                            stringResource(it),
                            Modifier.testTag("egg_feedback"),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
                TextButton(onClick = onClose) { Text(stringResource(R.string.close)) }
            }
        }
    }
}

internal fun launchEgg(context: Context, stage: EggStage): Int? {
    if (Build.VERSION.SDK_INT < stage.minimumApi) return R.string.egg_action_unavailable
    return try {
        val component = ComponentName(context.packageName, stage.className)
        if (stage.className.endsWith(".widget.PaintChipsActivity")) {
            context.packageManager.setComponentEnabledSetting(
                component,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP,
            )
        }
        context.startActivity(Intent().setComponent(component))
        null
    } catch (_: ActivityNotFoundException) {
        R.string.egg_action_unavailable
    } catch (_: SecurityException) {
        R.string.egg_action_unavailable
    }
}

@Composable
private fun EggIntegrationRows(integration: EggIntegration, onFeedback: (Int) -> Unit) {
    val context = LocalContext.current
    var enabled by
        remember(integration.className) {
            mutableStateOf(isEggIntegrationEnabled(context, integration))
        }
    DisposableEffect(context, integration.className) {
        val lifecycle = (context.getActivity() as? LifecycleOwner)?.lifecycle
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                enabled = isEggIntegrationEnabled(context, integration)
            }
        }
        lifecycle?.addObserver(observer)
        onDispose { lifecycle?.removeObserver(observer) }
    }
    val title = stringResource(eggIntegrationTitle(integration.kind))
    if (Build.VERSION.SDK_INT < integration.minimumApi) {
        SettingsListRow(
            title,
            value = stringResource(R.string.egg_requires_api, integration.minimumApi),
        )
    } else {
        SettingsListRow(
            title,
            tag = "egg_integration_${integration.className}",
            onClick = {
                requestEggIntegration(context, integration) { onFeedback(it) }
                enabled = isEggIntegrationEnabled(context, integration)
            },
        )
        if (enabled) {
            SettingsListRow(
                stringResource(R.string.egg_disable, title),
                onClick = {
                    setEggIntegrationEnabled(context, integration, false)
                    enabled = false
                    onFeedback(R.string.egg_integration_disabled)
                },
            )
        }
    }
}

private fun eggIntegrationTitle(kind: EggIntegrationKind) =
    when (kind) {
        EggIntegrationKind.DREAM -> R.string.egg_dream
        EggIntegrationKind.TILE -> R.string.egg_tile
        EggIntegrationKind.CONTROLS -> R.string.egg_controls
        EggIntegrationKind.WIDGET -> R.string.egg_widget
    }

internal fun eggStageTitle(stage: EggStage): Int =
    when {
        stage.title.startsWith("preview.") || stage.title.startsWith("beta.") ->
            if (stage.title.endsWith("ShruggyActivity")) R.string.egg_shrug
            else R.string.egg_preview
        stage.title == "Nyandroid" -> R.string.egg_nyandroid
        stage.title == "BeanBag" -> R.string.egg_beans
        stage.title == "DessertCase" -> R.string.egg_desserts
        stage.title == "LLandActivity" -> R.string.egg_flappy
        stage.title == "MLandActivity" -> R.string.egg_multiplayer
        stage.title == "neko.NekoLand" -> R.string.egg_cats
        stage.title == "octo.Ocquarium" -> R.string.egg_octopus
        stage.title == "paint.PaintActivity" -> R.string.egg_paint
        stage.title == "quares.QuaresActivity" -> R.string.egg_nonogram
        stage.title == "widget.PaintChipsActivity" -> R.string.egg_palette
        else -> R.string.egg_space
    }

private fun eggHelpResource(module: String): Int =
    when (module) {
        "Gingerbread" -> R.string.egg_help_g
        "Honeycomb" -> R.string.egg_help_h
        "IceCreamSandwich" -> R.string.egg_help_i
        "JellyBean" -> R.string.egg_help_j
        "KitKat" -> R.string.egg_help_k
        "Lollipop" -> R.string.egg_help_l
        "Marshmallow" -> R.string.egg_help_m
        "Nougat" -> R.string.egg_help_n
        "Oreo" -> R.string.egg_help_o
        "Pie" -> R.string.egg_help_p
        "Q" -> R.string.egg_help_q
        "R" -> R.string.egg_help_r
        "S" -> R.string.egg_help_s
        "Tiramisu" -> R.string.egg_help_t
        "UpsideDownCake" -> R.string.egg_help_u
        "VanillaIceCream" -> R.string.egg_help_v
        "Baklava" -> R.string.egg_help_b
        else -> R.string.egg_help_c
    }
