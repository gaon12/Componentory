package xyz.gaon.componentory.lab

import android.content.Context
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.viewinterop.AndroidView
import xyz.gaon.componentory.R

private const val LINE_COUNT = 24

// A bounded height is required so the real container actually clips and scrolls.
private const val SCROLL_HEIGHT_DP = 240

// Each horizontal line gets a fixed width so the row overflows the panel.
private const val HORIZONTAL_LINE_WIDTH_DP = 96

@Composable
internal fun PlatformScrollSample(
    family: PlatformFamily,
    component: LabComponent,
    viewId: Int,
    enabled: Boolean,
    modifier: Modifier,
) {
    val panel = if (viewId == R.id.sample_left) "LEFT" else "RIGHT"
    AndroidView(
        factory = { context ->
            val themed = family.createContext(context)
            val horizontal = component == LabComponent.HORIZONTAL_SCROLL_VIEW
            val lines =
                LinearLayout(themed).apply {
                    orientation = if (horizontal) LinearLayout.HORIZONTAL else LinearLayout.VERTICAL
                }
            repeat(LINE_COUNT) { index ->
                lines.addView(
                    TextView(themed).apply {
                        text = themed.getString(R.string.scroll_sample_line, index + 1)
                        if (horizontal)
                            layoutParams =
                                ViewGroup.LayoutParams(
                                    themed.dpToPx(HORIZONTAL_LINE_WIDTH_DP),
                                    ViewGroup.LayoutParams.WRAP_CONTENT,
                                )
                    }
                )
            }
            val scroll: ViewGroup =
                if (horizontal) HorizontalScrollView(themed) else ScrollView(themed)
            scroll.id = viewId
            scroll.addView(lines)
            LinearLayout(themed).apply {
                orientation = LinearLayout.VERTICAL
                addView(
                    scroll,
                    LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        themed.dpToPx(SCROLL_HEIGHT_DP),
                    ),
                )
            }
        },
        update = { root -> requireNotNull(root.findViewById<View>(viewId)).isEnabled = enabled },
        modifier = modifier.testTag("native_$panel"),
    )
}

private fun Context.dpToPx(dp: Int): Int =
    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), resources.displayMetrics)
        .toInt()
