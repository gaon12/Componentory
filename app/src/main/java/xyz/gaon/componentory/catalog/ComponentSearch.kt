package xyz.gaon.componentory.catalog

import android.content.Context
import xyz.gaon.componentory.lab.LabComponent

fun LabComponent.matchesSearch(query: String, context: Context? = null): Boolean {
    val term = query.trim()
    val names =
        listOfNotNull(
            label,
            context?.getString(labelRes),
            context?.getString(descriptionRes),
            platformSource,
            material2Function?.let { "androidx.compose.material.$it" },
            material3Function?.let { "androidx.compose.material3.$it" },
            expressiveFunction?.let { "androidx.compose.material3.$it" },
        ) +
            when (this) {
                LabComponent.TIME_PICKER_DIALOG ->
                    listOf("android.widget.TimePicker", "androidx.compose.material3.TimeInput")
                LabComponent.POPUP_MENU ->
                    listOf(
                        "androidx.compose.material.DropdownMenuItem",
                        "androidx.compose.material3.DropdownMenuItem",
                    )
                else -> emptyList()
            }
    return term.isEmpty() || names.any { it.contains(term, ignoreCase = true) }
}
