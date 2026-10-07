package xyz.gaon.componentory.lab

import android.content.Context
import android.util.TypedValue
import android.view.ContextThemeWrapper
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.Space
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import xyz.gaon.componentory.R

private const val LAYOUT_HEIGHT_DP = 140
private const val SPACE_GAP_DP = 48
private const val ABSOLUTE_CHILD_X_DP = 24
private const val ABSOLUTE_CHILD_Y_DP = 16

@Composable
internal fun PlatformLayoutSample(
    family: PlatformFamily,
    component: LabComponent,
    viewId: Int,
    enabled: Boolean,
    modifier: Modifier,
) {
    ReadableAndroidView(
        factory = { context ->
            val themed = family.createContext(context)
            val layout = createLayout(themed, component).apply { id = viewId }
            if (component == LabComponent.SPACE) {
                // Space only makes sense inside a row that shows the gap it keeps.
                LinearLayout(themed).apply {
                    orientation = LinearLayout.HORIZONTAL
                    gravity = Gravity.CENTER_VERTICAL
                    addView(layoutLabel(themed, 1))
                    addView(layout)
                    addView(layoutLabel(themed, 2))
                }
            } else {
                layout
            }
        },
        update = { root ->
            val layout = requireNotNull(root.findViewById<View>(viewId))
            layout.isEnabled = enabled
        },
        modifier =
            modifier.testTag(if (viewId == R.id.sample_left) "native_LEFT" else "native_RIGHT"),
    )
}

@Suppress("DEPRECATION")
private fun createLayout(themed: ContextThemeWrapper, component: LabComponent): View =
    when (component) {
        LabComponent.FRAME_LAYOUT ->
            FrameLayout(themed).apply {
                layoutParams = fixedHeight(themed)
                addView(
                    layoutLabel(themed, 1),
                    FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.WRAP_CONTENT,
                        FrameLayout.LayoutParams.WRAP_CONTENT,
                        Gravity.CENTER,
                    ),
                )
                addView(
                    layoutLabel(themed, 2),
                    FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.WRAP_CONTENT,
                        FrameLayout.LayoutParams.WRAP_CONTENT,
                        Gravity.TOP or Gravity.END,
                    ),
                )
            }
        LabComponent.LINEAR_LAYOUT ->
            LinearLayout(themed).apply {
                orientation = LinearLayout.HORIZONTAL
                repeat(3) { index ->
                    addView(
                        layoutLabel(themed, index + 1),
                        LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f),
                    )
                }
            }
        LabComponent.TABLE_LAYOUT ->
            TableLayout(themed).apply {
                repeat(2) { row ->
                    val tableRow = TableRow(themed)
                    repeat(2) { column ->
                        tableRow.addView(
                            TextView(themed).apply {
                                text = themed.getString(R.string.layout_cell, row + 1, column + 1)
                            }
                        )
                    }
                    addView(tableRow)
                }
            }
        LabComponent.GRID_LAYOUT ->
            GridLayout(themed).apply {
                columnCount = 2
                repeat(4) { index -> addView(layoutLabel(themed, index + 1)) }
            }
        LabComponent.RELATIVE_LAYOUT ->
            RelativeLayout(themed).apply {
                layoutParams = fixedHeight(themed)
                val anchored = layoutLabel(themed, 1).apply { id = View.generateViewId() }
                val below =
                    layoutLabel(themed, 2).apply {
                        layoutParams =
                            RelativeLayout.LayoutParams(
                                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                                )
                                .apply { addRule(RelativeLayout.BELOW, anchored.id) }
                    }
                addView(anchored)
                addView(below)
            }
        LabComponent.SPACE -> Space(themed).apply { layoutParams = spaceParams(themed) }
        else ->
            android.widget.AbsoluteLayout(themed).apply {
                layoutParams = fixedHeight(themed)
                addView(
                    layoutLabel(themed, 1),
                    android.widget.AbsoluteLayout.LayoutParams(
                        android.widget.AbsoluteLayout.LayoutParams.WRAP_CONTENT,
                        android.widget.AbsoluteLayout.LayoutParams.WRAP_CONTENT,
                        themed.dpToPx(ABSOLUTE_CHILD_X_DP),
                        themed.dpToPx(ABSOLUTE_CHILD_Y_DP),
                    ),
                )
                addView(
                    layoutLabel(themed, 2),
                    android.widget.AbsoluteLayout.LayoutParams(
                        android.widget.AbsoluteLayout.LayoutParams.WRAP_CONTENT,
                        android.widget.AbsoluteLayout.LayoutParams.WRAP_CONTENT,
                        themed.dpToPx(ABSOLUTE_CHILD_X_DP * 3),
                        themed.dpToPx(ABSOLUTE_CHILD_Y_DP * 3),
                    ),
                )
            }
    }

private fun layoutLabel(themed: ContextThemeWrapper, index: Int) =
    TextView(themed).apply { text = themed.getString(R.string.layout_child, index) }

private fun fixedHeight(themed: ContextThemeWrapper) =
    ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, themed.dpToPx(LAYOUT_HEIGHT_DP))

private fun spaceParams(themed: ContextThemeWrapper) =
    LinearLayout.LayoutParams(themed.dpToPx(SPACE_GAP_DP), ViewGroup.LayoutParams.WRAP_CONTENT)

private fun Context.dpToPx(dp: Int): Int =
    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), resources.displayMetrics)
        .toInt()
