package xyz.gaon.componentory.lab

import android.content.Context
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.Switch
import android.widget.TextView
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import xyz.gaon.componentory.R

internal const val SAMPLE_TEXT_MIN_SP = 16f

// These are readable current-OS samples, not captures of unmodified historical defaults.
@Composable
internal fun <T : View> ReadableAndroidView(
    factory: (Context) -> T,
    modifier: Modifier = Modifier,
    onReset: ((T) -> Unit)? = null,
    onRelease: (T) -> Unit = {},
    update: (T) -> Unit = {},
) {
    AndroidView(
        factory = { context ->
            factory(context).apply {
                if (this is Switch) setSwitchTextAppearance(this.context, R.style.SampleSwitchText)
                ensureReadableText()
                // Adapter rows and picker labels may arrive after the factory has returned.
                addOnAttachStateChangeListener(
                    object : View.OnAttachStateChangeListener {
                        private var tree: ViewTreeObserver? = null
                        private val layout =
                            ViewTreeObserver.OnGlobalLayoutListener { ensureReadableText() }

                        override fun onViewAttachedToWindow(view: View) {
                            tree = view.viewTreeObserver
                            tree?.addOnGlobalLayoutListener(layout)
                        }

                        override fun onViewDetachedFromWindow(view: View) {
                            tree?.takeIf { it.isAlive }?.removeOnGlobalLayoutListener(layout)
                            tree = null
                        }
                    }
                )
            }
        },
        modifier = modifier,
        onReset = onReset,
        onRelease = onRelease,
        update = { view ->
            update(view)
            view.ensureReadableText()
        },
    )
}

internal fun View.ensureReadableText() {
    if (this is TextView) {
        val minimum =
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP,
                SAMPLE_TEXT_MIN_SP,
                resources.displayMetrics,
            )
        if (textSize < minimum) setTextSize(TypedValue.COMPLEX_UNIT_SP, SAMPLE_TEXT_MIN_SP)
    }
    if (this is ViewGroup) {
        for (index in 0 until childCount) getChildAt(index).ensureReadableText()
    }
}
