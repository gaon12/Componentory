package xyz.gaon.componentory.lab

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.view.MotionEvent
import android.view.View
import android.widget.EdgeEffect
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.viewinterop.AndroidView

@Composable
internal fun PlatformEdgeEffectSample(
    family: PlatformFamily,
    component: LabComponent,
    viewId: Int,
    enabled: Boolean,
    state: SampleState,
    modifier: Modifier,
) {
    AndroidView(
        factory = { context ->
            val themed = family.createContext(context)
            EdgeEffectHostView(themed) { state.value += 1 }.apply { id = viewId }
        },
        update = { view -> view.isEnabled = enabled },
        modifier = modifier.testTag("sample-${component.name.lowercase()}"),
    )
}

// EdgeEffect is not a View; the documented usage is a View drawing the glow in
// onDraw while a gesture pulls it — the same pattern ScrollView uses
// internally. The host View therefore owns a real EdgeEffect on its tag and
// feeds genuine pull/release calls from MotionEvents.
@SuppressLint("ClickableViewAccessibility")
internal class EdgeEffectHostView(context: Context, private val onPull: () -> Unit) :
    View(context) {
    // EdgeEffect(Context) is deprecated at API 31 in favor of a factory that
    // needs a themed AttributeSet; the deprecated constructor keeps the sample
    // honest down to the widget's real minimum API 14.
    @Suppress("DEPRECATION") val edgeEffect = EdgeEffect(context)
    private var lastY = 0f

    init {
        minimumHeight = (96 * resources.displayMetrics.density).toInt()
        isClickable = true
        tag = edgeEffect
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isEnabled) return false
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> lastY = event.y
            MotionEvent.ACTION_MOVE -> {
                // onPull expects the gesture delta as a fraction of the edge.
                val dy = event.y - lastY
                lastY = event.y
                edgeEffect.onPull(dy / height)
                onPull()
                invalidate()
            }
            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_CANCEL -> {
                edgeEffect.onRelease()
                invalidate()
            }
        }
        return true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (!edgeEffect.isFinished) {
            edgeEffect.setSize(width, height)
            edgeEffect.draw(canvas)
            if (!edgeEffect.isFinished) invalidate()
        }
    }
}
