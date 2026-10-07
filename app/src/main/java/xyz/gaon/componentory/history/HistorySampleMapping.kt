package xyz.gaon.componentory.history

import xyz.gaon.componentory.lab.LabComponent

internal fun HistoricalComponent.currentSample(): LabComponent? =
    LabComponent.entries.firstOrNull { it.platformSource == name }
        ?: when (name) {
            "android.widget.TabWidget" -> LabComponent.TAB_HOST
            "android.widget.TableRow" -> LabComponent.TABLE_LAYOUT
            else -> null
        }

internal fun androidReferenceUrl(className: String): String {
    require(className.startsWith("android.")) { "An Android type is required." }
    val parts = className.split('.')
    val firstType = parts.indexOfFirst { it.firstOrNull()?.isUpperCase() == true }
    require(firstType > 0) { "A fully qualified Android type is required." }
    return "https://developer.android.com/reference/" +
        parts.take(firstType).joinToString("/") +
        "/" +
        parts.drop(firstType).joinToString(".")
}
