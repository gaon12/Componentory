package xyz.gaon.componentory.runs

import xyz.gaon.componentory.compare.ComparisonEntry
import xyz.gaon.componentory.compare.SampleSetup
import xyz.gaon.componentory.lab.DesignFamily
import xyz.gaon.componentory.lab.LabComponent

// savedValues() stores typed numbers and booleans while a run record keeps
// strings only. This registry names which keys hold which primitive type so a
// record restores real values instead of leaving "5" to fail an `as? Int`
// cast. Keys absent from the registry stay strings; restore() then ignores
// them, which keeps newer fields readable by older builds.
private object RunValues {
    private val intKeys = setOf("value", "rangeEnd", "time")
    private val longKeys =
        setOf("date", "inlineDate", "dateRangeStart", "dateRangeEnd", "chronometerBase")
    private val boolKeys =
        setOf("time24Hour", "containerClickable", "inlineDatePresent", "dateRangePresent")

    fun inputs(setup: SampleSetup): Map<String, String> =
        setup
            .savedValues()
            .filterKeys { it != "component" && it != "family" }
            .mapValues { (_, value) -> value.toString() }

    fun savedValues(
        component: LabComponent,
        family: DesignFamily,
        inputs: Map<String, String>,
    ): Map<String, Any> = buildMap {
        inputs.forEach { (key, value) ->
            when (key) {
                in intKeys -> value.toIntOrNull()?.let { put(key, it) }
                in longKeys -> value.toLongOrNull()?.let { put(key, it) }
                in boolKeys -> value.toBooleanStrictOrNull()?.let { put(key, it) }
                "component",
                "family" -> Unit // the record's top-level fields stay authoritative
                else -> put(key, value)
            }
        }
        put("component", component.name)
        put("family", family.name)
    }
}

// One screen comparison becomes one record. Both setups must describe the same
// component because a saved run is a single experiment, not two independent
// snapshots.
fun comparisonRecord(
    id: String,
    createdAtEpochMillis: Long,
    left: SampleSetup,
    right: SampleSetup,
    enabled: Boolean,
    environment: Map<String, String>,
): RunRecord {
    require(left.component == right.component) { "A run compares one component." }
    return RunRecord(
        id,
        createdAtEpochMillis,
        left.component.name,
        left.sourceFamily.name,
        right.sourceFamily.name,
        enabled,
        RunValues.inputs(left),
        RunValues.inputs(right),
        environment,
    )
}

// Reopening resolves names against the current catalog, so a record written by
// an older build simply refuses to open rather than guessing at a component.
fun RunRecord.toComparisonEntry(): ComparisonEntry? {
    val component = LabComponent.entries.firstOrNull { it.name == this.component } ?: return null
    val leftFamily = DesignFamily.entries.firstOrNull { it.name == this.leftFamily } ?: return null
    val rightFamily =
        DesignFamily.entries.firstOrNull { it.name == this.rightFamily } ?: return null
    val left =
        SampleSetup.restore(RunValues.savedValues(component, leftFamily, leftInputs)) ?: return null
    val right =
        SampleSetup.restore(RunValues.savedValues(component, rightFamily, rightInputs))
            ?: return null
    return ComparisonEntry(left, enabled, right)
}
