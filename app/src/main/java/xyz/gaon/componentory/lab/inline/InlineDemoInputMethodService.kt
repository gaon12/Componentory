package xyz.gaon.componentory.lab.inline

import android.graphics.Color
import android.inputmethodservice.InputMethodService
import android.os.Bundle
import android.util.Size
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InlineSuggestionsRequest
import android.view.inputmethod.InlineSuggestionsResponse
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.inline.InlineContentView
import android.widget.inline.InlinePresentationSpec
import androidx.annotation.RequiresApi
import androidx.autofill.inline.UiVersions
import androidx.autofill.inline.common.TextViewStyle
import androidx.autofill.inline.common.ViewStyle
import androidx.autofill.inline.v1.InlineSuggestionUi
import java.lang.ref.WeakReference
import xyz.gaon.componentory.R
import xyz.gaon.componentory.lab.PlatformFamily
import xyz.gaon.componentory.lab.ensureReadableText

@RequiresApi(30)
class InlineDemoInputMethodService : InputMethodService() {
    private var ownSample = false
    private var family = PlatformFamily.MATERIAL
    private var generation = 0
    private var strip: LinearLayout? = null
    private var content: InlineContentView? = null

    override fun onEvaluateFullscreenMode() = false

    override fun onCreate() {
        super.onCreate()
        InlineDemoSession.keyboard = WeakReference(this)
    }

    override fun onStartInput(attribute: EditorInfo, restarting: Boolean) {
        super.onStartInput(attribute, restarting)
        InlineDemoSession.setKeyboardReady(false)
        clearContent()
        // Never read, forward, or handle input from another app or another editor.
        ownSample =
            attribute.packageName == packageName &&
                attribute.extras?.getBoolean(InlineDemoSession.EDITOR_MARKER) == true
        family =
            PlatformFamily.entries.firstOrNull {
                it.name == attribute.extras?.getString(InlineDemoSession.EDITOR_FAMILY)
            } ?: PlatformFamily.MATERIAL
        if (ownSample) InlineDemoSession.reset()
        if (strip != null) setInputView(onCreateInputView())
    }

    override fun onStartInputView(info: EditorInfo, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        InlineDemoSession.setKeyboardReady(ownSample)
    }

    override fun onFinishInputView(finishingInput: Boolean) {
        InlineDemoSession.setKeyboardReady(false)
        super.onFinishInputView(finishingInput)
    }

    override fun onCreateInputView(): View {
        val themed = family.createContext(this)
        val spacing = dp(12)
        return LinearLayout(themed).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(spacing, spacing, spacing, spacing)
            setBackgroundColor(Color.WHITE)
            addView(
                TextView(themed).apply {
                    setText(
                        if (ownSample) R.string.inline_keyboard_title
                        else R.string.inline_keyboard_outside_sample
                    )
                }
            )
            strip =
                LinearLayout(themed).apply {
                    id = R.id.inline_sample_strip
                    minimumHeight = dp(56)
                }
            addView(requireNotNull(strip))
            content?.let { view ->
                (view.parent as? ViewGroup)?.removeView(view)
                strip?.addView(view)
                view.post { recordAttached(view) }
            }
            val controls = LinearLayout(themed)
            controls.addView(
                Button(themed).apply {
                    id = R.id.inline_sample_attach
                    isEnabled = content != null
                    setText(R.string.inline_attach_toggle)
                    setOnClickListener {
                        content?.let { view ->
                            if (view.parent != null) {
                                strip?.removeView(view)
                                InlineDemoSession.update(
                                    InlineDemoStatus(
                                        InlineDemoPhase.DETACHED,
                                        view.javaClass.name,
                                        view.width,
                                        view.height,
                                    )
                                )
                            } else {
                                strip?.addView(view)
                                view.post { recordAttached(view) }
                            }
                        }
                    }
                },
                LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f),
            )
            controls.addView(
                Button(themed).apply {
                    id = R.id.inline_sample_surface
                    isEnabled = content != null
                    setText(R.string.inline_surface_toggle)
                    setOnClickListener { content?.let { it.setZOrderedOnTop(!it.isZOrderedOnTop) } }
                },
                LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f),
            )
            addView(controls)
            addView(
                Button(themed).apply {
                    id = R.id.inline_previous_keyboard
                    setText(R.string.inline_previous_keyboard)
                    setOnClickListener { switchToPreviousInputMethod() }
                }
            )
            ensureReadableText()
        }
    }

    override fun onCreateInlineSuggestionsRequest(uiExtras: Bundle): InlineSuggestionsRequest? {
        if (
            !ownSample ||
                !getSystemService(android.view.autofill.AutofillManager::class.java)
                    .hasEnabledAutofillServices() ||
                !UiVersions.getVersions(uiExtras).contains(UiVersions.INLINE_UI_VERSION_1)
        )
            return null
        val style =
            InlineSuggestionUi.newStyleBuilder()
                .setChipStyle(ViewStyle.Builder().build())
                .setTitleStyle(
                    TextViewStyle.Builder().setTextSize(16f).setTextColor(Color.BLACK).build()
                )
                .build()
        val styles = UiVersions.newStylesBuilder().addStyle(style).build()
        val spec =
            InlinePresentationSpec.Builder(
                    Size(dp(48), dp(48)),
                    Size(resources.displayMetrics.widthPixels - dp(24), dp(64)),
                )
                .setStyle(styles)
                .build()
        return InlineSuggestionsRequest.Builder(listOf(spec)).setMaxSuggestionCount(1).build()
    }

    override fun onInlineSuggestionsResponse(response: InlineSuggestionsResponse): Boolean {
        clearContent()
        if (
            !ownSample ||
                !getSystemService(android.view.autofill.AutofillManager::class.java)
                    .hasEnabledAutofillServices()
        )
            return false
        val suggestion = response.inlineSuggestions.firstOrNull()
        if (suggestion == null) {
            InlineDemoSession.update(InlineDemoStatus(InlineDemoPhase.EMPTY))
            return true
        }
        val requestedGeneration = generation
        // The platform constructs the actual InlineContentView; no hidden constructor or fixture is
        // used.
        suggestion.inflate(
            family.createContext(this),
            Size(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT),
            mainExecutor,
        ) { view ->
            if (requestedGeneration != generation || !ownSample) return@inflate
            if (view == null) {
                InlineDemoSession.update(InlineDemoStatus(InlineDemoPhase.EMPTY))
                return@inflate
            }
            content = view
            view.id = R.id.inline_sample_content
            strip?.addView(view)
            setControlsEnabled(true)
            view.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ -> recordAttached(view) }
            view.post { recordAttached(view) }
        }
        return true
    }

    private fun recordAttached(view: InlineContentView) {
        if (content === view && view.parent != null && view.isAttachedToWindow)
            InlineDemoSession.update(
                InlineDemoStatus(
                    InlineDemoPhase.ATTACHED,
                    view.javaClass.name,
                    view.width,
                    view.height,
                )
            )
    }

    private fun clearContent() {
        generation++
        strip?.removeAllViews()
        content = null
        setControlsEnabled(false)
    }

    private fun setControlsEnabled(enabled: Boolean) {
        listOf(R.id.inline_sample_attach, R.id.inline_sample_surface).forEach { id ->
            strip?.rootView?.findViewById<View>(id)?.isEnabled = enabled
        }
    }

    override fun onFinishInput() {
        InlineDemoSession.setKeyboardReady(false)
        clearContent()
        ownSample = false
        super.onFinishInput()
    }

    override fun onDestroy() {
        InlineDemoSession.setKeyboardReady(false)
        clearContent()
        if (InlineDemoSession.keyboard.get() === this) InlineDemoSession.keyboard.clear()
        super.onDestroy()
    }

    internal fun returnToPreviousKeyboard() {
        if (ownSample && isInputViewShown) switchToPreviousInputMethod()
    }

    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()
}
