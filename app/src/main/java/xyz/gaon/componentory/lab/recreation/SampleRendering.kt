package xyz.gaon.componentory.lab.recreation

import xyz.gaon.componentory.R
import xyz.gaon.componentory.lab.DesignFamily
import xyz.gaon.componentory.lab.LabComponent
import xyz.gaon.componentory.lab.usesThemedClock

internal enum class SampleRendering(val title: Int, val description: Int) {
    RESOURCE_RECREATION(R.string.rendering_resources, R.string.rendering_resources_note),
    CURRENT_OS(R.string.rendering_current_os, R.string.rendering_current_os_note),
    LIBRARY(R.string.rendering_library, R.string.rendering_library_note),
    THEMED_DEMO(R.string.rendering_themed_demo, R.string.rendering_themed_demo_note),
}

internal fun sampleRendering(family: DesignFamily, component: LabComponent): SampleRendering =
    if (component == LabComponent.TOAST) SampleRendering.RESOURCE_RECREATION
    else if (family.usesThemedClock(component)) SampleRendering.THEMED_DEMO
    else
        family.platform?.let {
            if (HistoricalControls.supported(it, component)) SampleRendering.RESOURCE_RECREATION
            else SampleRendering.CURRENT_OS
        } ?: SampleRendering.LIBRARY

internal fun resourceRelease(family: DesignFamily, component: LabComponent): String =
    if (component == LabComponent.TOAST) ResourceToasts.forFamily(family).release
    else HistoricalControls.release(requireNotNull(family.platform))

internal fun renderingSnapshot(
    family: DesignFamily,
    component: LabComponent,
    side: String,
): Map<String, String> {
    val fields = mutableMapOf("${side}Rendering" to sampleRendering(family, component).name)
    if (family.usesThemedClock(component)) {
        fields["${side}InteractionEngine"] = "COMPONENTORY_COMPOSE"
        fields["${side}OriginalCapture"] = "NOT_APPLICABLE"
        fields["${side}TextMinimumSp"] = "16"
        return fields
    }
    if (component == LabComponent.TOAST) {
        val release = ResourceToasts.forFamily(family)
        fields["${side}ResourceRelease"] = release.release
        fields["${side}ResourceCommit"] = release.commit
        fields["${side}InteractionEngine"] = "COMPONENTORY_POPUP"
        fields["${side}OriginalCapture"] = "MISSING"
        fields["${side}TextMinimumSp"] = "16"
        return fields
    }
    family.platform
        ?.takeIf { HistoricalControls.supported(it, component) }
        ?.let {
            fields["${side}ResourceRelease"] = HistoricalControls.release(it)
            fields["${side}ResourceCommit"] = HistoricalControls.commit(it)
            fields["${side}TextMinimumSp"] = "16"
        }
    return fields
}
