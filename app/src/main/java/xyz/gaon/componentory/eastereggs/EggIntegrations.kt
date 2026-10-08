package xyz.gaon.componentory.eastereggs

import android.app.StatusBarManager
import android.appwidget.AppWidgetManager
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Icon
import android.os.Build
import android.provider.Settings
import xyz.gaon.componentory.R
import xyz.gaon.componentory.eastereggs.port.R as PortR

internal fun isEggIntegrationEnabled(context: Context, integration: EggIntegration): Boolean =
    context.packageManager.getComponentEnabledSetting(
        ComponentName(context.packageName, integration.className)
    ) == PackageManager.COMPONENT_ENABLED_STATE_ENABLED

internal fun setEggIntegrationEnabled(
    context: Context,
    integration: EggIntegration,
    enabled: Boolean,
) {
    context.packageManager.setComponentEnabledSetting(
        ComponentName(context.packageName, integration.className),
        if (enabled) PackageManager.COMPONENT_ENABLED_STATE_ENABLED
        else PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
        PackageManager.DONT_KILL_APP,
    )
}

internal fun requestEggIntegration(
    context: Context,
    integration: EggIntegration,
    onFeedback: (Int) -> Unit,
) {
    if (Build.VERSION.SDK_INT < integration.minimumApi) {
        onFeedback(R.string.egg_action_unavailable)
        return
    }
    try {
        setEggIntegrationEnabled(context, integration, true)
        val component = ComponentName(context.packageName, integration.className)
        when (integration.kind) {
            EggIntegrationKind.DREAM -> {
                context.startActivity(Intent(Settings.ACTION_DREAM_SETTINGS))
                onFeedback(R.string.egg_dream_choose)
            }
            EggIntegrationKind.CONTROLS -> onFeedback(R.string.egg_controls_choose)
            EggIntegrationKind.TILE -> {
                if (Build.VERSION.SDK_INT >= 33) {
                    val manager = context.getSystemService(StatusBarManager::class.java)
                    if (manager == null) onFeedback(R.string.egg_action_unavailable)
                    else
                        manager.requestAddTileService(
                            component,
                            context.getString(R.string.egg_cats),
                            Icon.createWithResource(context, PortR.drawable.n_stat_icon),
                            context.mainExecutor,
                        ) { result ->
                            onFeedback(
                                if (
                                    result == StatusBarManager.TILE_ADD_REQUEST_RESULT_TILE_ADDED ||
                                        result ==
                                            StatusBarManager
                                                .TILE_ADD_REQUEST_RESULT_TILE_ALREADY_ADDED
                                )
                                    R.string.egg_tile_added
                                else R.string.egg_tile_choose
                            )
                        }
                } else onFeedback(R.string.egg_tile_choose)
            }
            EggIntegrationKind.WIDGET -> {
                if (Build.VERSION.SDK_INT >= 31) {
                    val manager = AppWidgetManager.getInstance(context)
                    if (
                        manager.isRequestPinAppWidgetSupported &&
                            manager.requestPinAppWidget(component, null, null)
                    ) {
                        onFeedback(R.string.egg_widget_confirm)
                    } else onFeedback(R.string.egg_widget_choose)
                } else onFeedback(R.string.egg_action_unavailable)
            }
        }
    } catch (_: ActivityNotFoundException) {
        onFeedback(R.string.egg_action_unavailable)
    } catch (_: SecurityException) {
        onFeedback(R.string.egg_action_unavailable)
    } catch (_: IllegalArgumentException) {
        onFeedback(R.string.egg_action_unavailable)
    }
}
