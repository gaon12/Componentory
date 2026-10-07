package xyz.gaon.componentory.lab

import android.content.Context
import android.util.TypedValue
import android.view.ContextThemeWrapper
import android.view.Gravity
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageSwitcher
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextSwitcher
import android.widget.TextView
import android.widget.ViewAnimator
import android.widget.ViewFlipper
import android.widget.ViewSwitcher
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import xyz.gaon.componentory.R

private const val SWITCHER_HEIGHT_DP = 160
private val switcherImages =
    intArrayOf(android.R.drawable.btn_star_big_on, android.R.drawable.btn_star_big_off)

// The index a switcher last applied; lets restore set the child without an
// animation while button taps still use the widget's animated transitions.
private class SwitcherSync(var index: Int = -1)

@Composable
internal fun PlatformSwitcherSample(
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
            val holder = SwitcherSync()
            val animator = createSwitcher(themed, component).apply { id = viewId }
            val controls =
                LinearLayout(themed).apply {
                    orientation = LinearLayout.HORIZONTAL
                    addView(
                        switcherAction(themed, "switcher_prev_$panel", R.string.switcher_previous) {
                            stepSwitcher(
                                themed,
                                animator,
                                component.switcherPageCount,
                                holder,
                                -1,
                                state,
                            )
                        }
                    )
                    addView(
                        switcherAction(themed, "switcher_next_$panel", R.string.switcher_next) {
                            stepSwitcher(
                                themed,
                                animator,
                                component.switcherPageCount,
                                holder,
                                1,
                                state,
                            )
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
                        themed.dpToPx(SWITCHER_HEIGHT_DP),
                    ),
                )
                addView(controls)
            }
        },
        update = { root ->
            val holder = root.tag as SwitcherSync
            val animator = requireNotNull(root.findViewById<ViewAnimator>(viewId))
            val target = state.value.coerceIn(0, component.switcherPageCount - 1)
            if (holder.index != target) {
                applySwitcherChild(root.context, animator, target)
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

private fun createSwitcher(themed: ContextThemeWrapper, component: LabComponent): ViewAnimator =
    when (component) {
        LabComponent.TEXT_SWITCHER ->
            TextSwitcher(themed).apply {
                setFactory { switcherPage(themed) }
                setCurrentText(themed.getString(R.string.switcher_line, 1))
            }
        LabComponent.IMAGE_SWITCHER ->
            ImageSwitcher(themed).apply {
                setFactory { ImageView(themed).apply { scaleType = ImageView.ScaleType.CENTER } }
                setImageResource(switcherImages[0])
            }
        else ->
            (when (component) {
                    LabComponent.VIEW_ANIMATOR -> ViewAnimator(themed)
                    LabComponent.VIEW_FLIPPER -> ViewFlipper(themed)
                    else -> ViewSwitcher(themed)
                })
                .apply {
                    repeat(component.switcherPageCount) { index ->
                        val page = switcherPage(themed)
                        page.text = themed.getString(R.string.switcher_page, index + 1)
                        addView(page)
                    }
                }
    }.apply {
        // The widgets ship no transition when built in code; the framework
        // fade animations keep the step visible and still come from AOSP.
        inAnimation = AnimationUtils.loadAnimation(themed, android.R.anim.fade_in)
        outAnimation = AnimationUtils.loadAnimation(themed, android.R.anim.fade_out)
    }

private fun stepSwitcher(
    context: Context,
    animator: ViewAnimator,
    count: Int,
    holder: SwitcherSync,
    delta: Int,
    state: SampleState,
) {
    val target = ((state.value + delta) % count + count) % count
    when (animator) {
        is TextSwitcher -> animator.setText(context.getString(R.string.switcher_line, target + 1))
        is ImageSwitcher -> animator.setImageResource(switcherImages[target % switcherImages.size])
        else -> if (delta > 0) animator.showNext() else animator.showPrevious()
    }
    holder.index = target
    state.value = target
}

private fun applySwitcherChild(context: Context, animator: ViewAnimator, target: Int) {
    when (animator) {
        is TextSwitcher ->
            animator.setCurrentText(context.getString(R.string.switcher_line, target + 1))
        is ImageSwitcher -> animator.setImageResource(switcherImages[target % switcherImages.size])
        else -> animator.displayedChild = target
    }
}

private fun switcherPage(themed: ContextThemeWrapper) =
    TextView(themed).apply {
        gravity = Gravity.CENTER
        layoutParams =
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT,
            )
    }

private fun switcherAction(context: Context, tag: String, textRes: Int, action: () -> Unit) =
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
