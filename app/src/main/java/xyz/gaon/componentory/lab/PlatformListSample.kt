package xyz.gaon.componentory.lab

import android.util.TypedValue
import android.view.ContextThemeWrapper
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.ArrayAdapter
import android.widget.BaseExpandableListAdapter
import android.widget.CheckedTextView
import android.widget.ExpandableListView
import android.widget.GridView
import android.widget.ListView
import android.widget.TextView
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import xyz.gaon.componentory.R

private const val LIST_HEIGHT_DP = 240

// The checked/expanded state last written to the widget so updates only run
// when the copied state actually changes.
private class ListSync(var value: Int = -1)

@Composable
internal fun PlatformListSample(
    family: PlatformFamily,
    component: LabComponent,
    viewId: Int,
    enabled: Boolean,
    state: SampleState,
    modifier: Modifier,
) {
    val panel = if (viewId == R.id.sample_left) "LEFT" else "RIGHT"
    ReadableAndroidView(
        factory = { context ->
            val themed = family.createContext(context)
            createAdapterView(themed, component, state).apply {
                id = viewId
                tag = ListSync()
            }
        },
        update = { view ->
            val list = view as AbsListView
            val holder = list.tag as ListSync
            if (holder.value != state.value) {
                applyListValue(list, component, state.value)
                holder.value = state.value
            }
            list.isEnabled = enabled
        },
        modifier = modifier.testTag("native_$panel"),
    )
}

private fun createAdapterView(
    themed: ContextThemeWrapper,
    component: LabComponent,
    state: SampleState,
): AbsListView =
    when (component) {
        LabComponent.EXPANDABLE_LIST_VIEW ->
            ExpandableListView(themed).apply {
                layoutParams = fixedHeight(themed)
                setAdapter(SampleExpandableAdapter(themed, component.listRowCount))
                setOnGroupExpandListener { syncExpanded(this, state) }
                setOnGroupCollapseListener { syncExpanded(this, state) }
            }
        else ->
            (if (component == LabComponent.GRID_VIEW) GridView(themed).apply { numColumns = 3 }
                else ListView(themed).apply { layoutParams = fixedHeight(themed) })
                .apply {
                    adapter = choiceAdapter(themed, component.listRowCount)
                    choiceMode = AbsListView.CHOICE_MODE_SINGLE
                    setOnItemClickListener { parent, _, position, _ ->
                        // A tap on the checked row unchecks it; mirror that in state.
                        state.value =
                            if ((parent as AbsListView).isItemChecked(position)) position + 1 else 0
                    }
                }
    }

private fun applyListValue(list: AbsListView, component: LabComponent, value: Int) {
    if (list is ExpandableListView) {
        (0 until component.listRowCount).forEach { group ->
            if (value and (1 shl group) != 0) list.expandGroup(group) else list.collapseGroup(group)
        }
    } else {
        (0 until component.listRowCount).forEach { position ->
            list.setItemChecked(position, value == position + 1)
        }
    }
}

private fun syncExpanded(list: ExpandableListView, state: SampleState) {
    val count = list.expandableListAdapter.groupCount
    state.value =
        (0 until count).fold(0) { bits, group ->
            if (list.isGroupExpanded(group)) bits or (1 shl group) else bits
        }
}

private fun choiceAdapter(themed: ContextThemeWrapper, count: Int) =
    ArrayAdapter(
        themed,
        android.R.layout.simple_list_item_single_choice,
        (1..count).map { themed.getString(R.string.list_item, it) },
    )

private class SampleExpandableAdapter(
    private val themed: ContextThemeWrapper,
    private val groups: Int,
) : BaseExpandableListAdapter() {
    override fun getGroupCount() = groups

    override fun getChildrenCount(group: Int) = 2

    override fun getGroup(group: Int) = group

    override fun getChild(group: Int, child: Int) = group * 2 + child

    override fun getGroupId(group: Int) = group.toLong()

    override fun getChildId(group: Int, child: Int) = (group * 2 + child).toLong()

    override fun hasStableIds() = true

    override fun isChildSelectable(group: Int, child: Int) = true

    override fun getGroupView(
        group: Int,
        expanded: Boolean,
        convertView: View?,
        parent: ViewGroup,
    ): View =
        (convertView as? TextView ?: TextView(themed)).apply {
            text = themed.getString(R.string.list_group, group + 1)
            setPadding(dpToPx(48), dpToPx(12), dpToPx(12), dpToPx(12))
        }

    override fun getChildView(
        group: Int,
        child: Int,
        lastChild: Boolean,
        convertView: View?,
        parent: ViewGroup,
    ): View =
        (convertView as? CheckedTextView ?: CheckedTextView(themed)).apply {
            text = themed.getString(R.string.layout_child, group * 2 + child + 1)
            setPadding(dpToPx(72), dpToPx(8), dpToPx(12), dpToPx(8))
        }

    private fun dpToPx(dp: Int): Int =
        TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp.toFloat(),
                themed.resources.displayMetrics,
            )
            .toInt()
}

private fun fixedHeight(themed: ContextThemeWrapper) =
    ViewGroup.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                LIST_HEIGHT_DP.toFloat(),
                themed.resources.displayMetrics,
            )
            .toInt(),
    )
