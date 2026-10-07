package xyz.gaon.componentory.lab

import android.animation.AnimatorInflater
import android.animation.ObjectAnimator
import android.content.Context
import android.util.TypedValue
import android.view.ContextThemeWrapper
import android.widget.AdapterViewAnimator
import android.widget.AdapterViewFlipper
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.StackView
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import xyz.gaon.componentory.R

private const val ANIMATOR_HEIGHT_DP = 200

// The index an animator last applied; lets restore set the child without an
// animation while button taps still use the widget's animated transitions.
private class AnimatorSync(var index: Int = -1)

@Composable
internal fun PlatformAdapterAnimatorSample(
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
            val holder = AnimatorSync()
            val animator = createAnimator(themed, component).apply { id = viewId }
            val controls =
                LinearLayout(themed).apply {
                    orientation = LinearLayout.HORIZONTAL
                    addView(
                        animatorAction(themed, "animator_prev_$panel", R.string.switcher_previous) {
                            stepAnimator(animator, component.adapterPageCount, holder, -1, state)
                        }
                    )
                    addView(
                        animatorAction(themed, "animator_next_$panel", R.string.switcher_next) {
                            stepAnimator(animator, component.adapterPageCount, holder, 1, state)
                        }
                    )
                }
            LinearLayout(themed).apply {
                tag = holder
                orientation = LinearLayout.VERTICAL
                addView(
                    animator,
                    LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        themed.dpToPx(ANIMATOR_HEIGHT_DP),
                    ),
                )
                addView(controls)
            }
        },
        update = { root ->
            val holder = root.tag as AnimatorSync
            val animator = requireNotNull(root.findViewById<AdapterViewAnimator>(viewId))
            val target = state.value.coerceIn(0, component.adapterPageCount - 1)
            if (holder.index != target) {
                animator.displayedChild = target
                holder.index = target
            }
            animator.isEnabled = enabled
            val controls = root.getChildAt(1) as LinearLayout
            controls.getChildAt(0).isEnabled = enabled
            controls.getChildAt(1).isEnabled = enabled
        },
        modifier = modifier.testTag("native_$panel"),
    )
}

private fun createAnimator(themed: ContextThemeWrapper, component: LabComponent) =
    (when (component) {
            LabComponent.STACK_VIEW -> StackView(themed)
            else -> AdapterViewFlipper(themed)
        })
        .apply {
            setAdapter(pageAdapter(themed, component))
            // The widgets ship no transition when built in code; the framework
            // fade animators keep the step visible and still come from AOSP.
            inAnimation =
                AnimatorInflater.loadAnimator(themed, android.R.animator.fade_in) as ObjectAnimator
            outAnimation =
                AnimatorInflater.loadAnimator(themed, android.R.animator.fade_out) as ObjectAnimator
        }

private fun pageAdapter(themed: ContextThemeWrapper, component: LabComponent) =
    ArrayAdapter(
        themed,
        android.R.layout.simple_list_item_1,
        (1..component.adapterPageCount).map { themed.getString(R.string.switcher_page, it) },
    )

private fun stepAnimator(
    animator: AdapterViewAnimator,
    count: Int,
    holder: AnimatorSync,
    delta: Int,
    state: SampleState,
) {
    val target = ((state.value + delta) % count + count) % count
    if (delta > 0) animator.showNext() else animator.showPrevious()
    holder.index = target
    state.value = target
}

private fun animatorAction(context: Context, tag: String, textRes: Int, action: () -> Unit) =
    Button(context).apply {
        setText(textRes)
        this.tag = tag
        contentDescription = context.getString(textRes)
        setOnClickListener { action() }
        layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
    }

private fun Context.dpToPx(dp: Int): Int =
    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), resources.displayMetrics)
        .toInt()
