package xyz.gaon.componentory.navigation

import xyz.gaon.componentory.lab.DesignFamily
import xyz.gaon.componentory.lab.LabComponent

internal fun selectDetailProvider(
    component: LabComponent,
    current: DesignFamily,
    remembered: DesignFamily?,
    runtimeApi: Int,
): DesignFamily {
    // An explicit choice can intentionally explore an unsupported provider.
    if (remembered != null) return remembered
    if (current.unsupportedReason(component, runtimeApi) == null) return current
    return DesignFamily.entries.firstOrNull { it.unsupportedReason(component, runtimeApi) == null }
        ?: current
}
