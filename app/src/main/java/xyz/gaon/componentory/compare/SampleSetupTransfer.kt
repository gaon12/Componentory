package xyz.gaon.componentory.compare

import android.content.Context
import android.os.Build
import xyz.gaon.componentory.icons.IconCatalog
import xyz.gaon.componentory.lab.DesignFamily
import xyz.gaon.componentory.lab.LabComponent
import xyz.gaon.componentory.lab.SampleState

internal fun captureSampleSetup(
    context: Context,
    component: LabComponent,
    family: DesignFamily,
    state: SampleState,
): SampleSetup {
    val supported = family.unsupportedReason(component, Build.VERSION.SDK_INT) == null
    val displayedIcon =
        if (component.usesIcon && supported) {
            IconCatalog.selected(context, family.platform != null, state.icon)
                .takeIf { it.available }
                ?.id
        } else null
    return SampleSetup.capture(component, family, state, Build.VERSION.SDK_INT, displayedIcon)
}

internal fun copySampleSetup(
    context: Context,
    setup: SampleSetup,
    targetFamily: DesignFamily,
    targetState: SampleState,
): SetupCopyResult {
    val supported = targetFamily.unsupportedReason(setup.component, Build.VERSION.SDK_INT) == null
    val targetIcon =
        if (setup.component.usesIcon && supported) {
            IconCatalog.selected(context, targetFamily.platform != null, targetState.icon).id
        } else ""
    // Membership and availability must match the exact ID; selected() would silently fall back.
    val iconAvailable =
        setup.iconId != null &&
            supported &&
            (setup.sourceFamily.platform != null) == (targetFamily.platform != null) &&
            IconCatalog.entries(context, targetFamily.platform != null).any {
                it.id == setup.iconId && it.available
            }
    return setup.copyTo(targetFamily, Build.VERSION.SDK_INT, iconAvailable, targetIcon)
}
