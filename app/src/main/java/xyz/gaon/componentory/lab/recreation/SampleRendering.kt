package xyz.gaon.componentory.lab.recreation

import xyz.gaon.componentory.R
import xyz.gaon.componentory.lab.DesignFamily
import xyz.gaon.componentory.lab.LabComponent

internal enum class SampleRendering(val title: Int, val description: Int) {
    RESOURCE_RECREATION(R.string.rendering_resources, R.string.rendering_resources_note),
    CURRENT_OS(R.string.rendering_current_os, R.string.rendering_current_os_note),
    LIBRARY(R.string.rendering_library, R.string.rendering_library_note),
}

internal fun sampleRendering(family: DesignFamily, component: LabComponent): SampleRendering =
    family.platform?.let {
        if (HistoricalControls.supported(it, component)) SampleRendering.RESOURCE_RECREATION
        else SampleRendering.CURRENT_OS
    } ?: SampleRendering.LIBRARY

internal fun renderingSnapshot(
    family: DesignFamily,
    component: LabComponent,
    side: String,
): Map<String, String> {
    val fields = mutableMapOf("${side}Rendering" to sampleRendering(family, component).name)
    family.platform
        ?.takeIf { HistoricalControls.supported(it, component) }
        ?.let {
            fields["${side}ResourceRelease"] = HistoricalControls.release(it)
            fields["${side}ResourceCommit"] = HistoricalControls.commit(it)
            fields["${side}TextMinimumSp"] = "16"
        }
    return fields
}
