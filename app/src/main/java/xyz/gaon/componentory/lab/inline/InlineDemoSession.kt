package xyz.gaon.componentory.lab.inline

import java.lang.ref.WeakReference
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

internal enum class InlineDemoPhase {
    WAITING,
    EMPTY,
    ATTACHED,
    DETACHED,
}

internal data class InlineDemoStatus(
    val phase: InlineDemoPhase,
    val className: String = "",
    val width: Int = 0,
    val height: Int = 0,
)

// Only fixed demo values and the public host's lifecycle are shared between our own screens.
internal object InlineDemoSession {
    const val DEMO_VALUE = "componentory.demo"
    const val EDITOR_MARKER = "xyz.gaon.componentory.inlineDemo"
    const val EDITOR_FAMILY = "xyz.gaon.componentory.inlineFamily"
    private val current = MutableStateFlow(InlineDemoStatus(InlineDemoPhase.WAITING))
    val status = current.asStateFlow()
    private val ready = MutableStateFlow(false)
    val keyboardReady = ready.asStateFlow()

    fun setKeyboardReady(value: Boolean) {
        ready.value = value
    }

    var keyboard = WeakReference<InlineDemoInputMethodService>(null)

    fun update(status: InlineDemoStatus) {
        current.value = status
    }

    fun reset() {
        update(InlineDemoStatus(InlineDemoPhase.WAITING))
    }
}
