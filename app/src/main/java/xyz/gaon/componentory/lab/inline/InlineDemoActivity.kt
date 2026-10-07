package xyz.gaon.componentory.lab.inline

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.autofill.AutofillManager
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.annotation.RequiresApi
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch
import xyz.gaon.componentory.BuildConfig
import xyz.gaon.componentory.R
import xyz.gaon.componentory.lab.PlatformFamily
import xyz.gaon.componentory.settings.LanguagePreferences

@RequiresApi(30)
class InlineDemoActivity : ComponentActivity() {
    private var inflated = false
    private var requestPending = false

    override fun attachBaseContext(newBase: Context) {
        val localized = LanguagePreferences.localizedContext(newBase)
        val configuration =
            Configuration(localized.resources.configuration).apply {
                uiMode =
                    (uiMode and Configuration.UI_MODE_NIGHT_MASK.inv()) or
                        Configuration.UI_MODE_NIGHT_NO
            }
        super.attachBaseContext(localized.createConfigurationContext(configuration))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val family =
            PlatformFamily.entries.firstOrNull { it.name == intent.getStringExtra(EXTRA_FAMILY) }
                ?: PlatformFamily.MATERIAL
        setTheme(family.themeId)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)
        InlineDemoSession.reset()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        inflated =
            savedInstanceState?.getBoolean(EXTRA_INFLATED)
                ?: intent.getBooleanExtra(EXTRA_INFLATED, false)
        val spacing = (16 * resources.displayMetrics.density).toInt()
        val body =
            LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(spacing, spacing, spacing, spacing)
            }
        fun label(text: String): TextView =
            TextView(this).apply {
                this.text = text
                textSize = 16f
                body.addView(this)
            }
        label(getString(R.string.component_inline_content_view))
        label(getString(R.string.component_inline_content_view_description))
        label(getString(R.string.runtime_sample_note, android.os.Build.VERSION.RELEASE))
        label(getString(R.string.inline_setup_note))
        label(
            "android.widget.inline.InlineContentView\nandroid:${family.themeName}\nandroidx.autofill:autofill:${BuildConfig.AUTOFILL_VERSION} · inline UI v1"
        )
        val status =
            label(getString(R.string.inline_status_waiting)).apply {
                id = R.id.inline_sample_status
            }
        fun action(label: Int, id: Int = View.NO_ID, onClick: () -> Unit) {
            body.addView(
                Button(this).apply {
                    this.id = id
                    setText(label)
                    textSize = 16f
                    setOnClickListener { onClick() }
                }
            )
        }
        action(R.string.inline_choose_autofill) {
            startActivity(
                Intent(
                    Settings.ACTION_REQUEST_SET_AUTOFILL_SERVICE,
                    Uri.parse("package:$packageName"),
                )
            )
        }
        action(R.string.inline_enable_keyboard) {
            startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
        }
        action(R.string.inline_choose_keyboard) {
            getSystemService(InputMethodManager::class.java).showInputMethodPicker()
        }
        // Editor extras identify this fixed-value demo to our own keyboard.
        val inputWithExtras =
            object : EditText(this) {
                    override fun onCreateInputConnection(
                        outAttrs: android.view.inputmethod.EditorInfo
                    ): android.view.inputmethod.InputConnection? {
                        val connection = super.onCreateInputConnection(outAttrs)
                        outAttrs.extras =
                            (outAttrs.extras ?: Bundle()).apply {
                                putBoolean(InlineDemoSession.EDITOR_MARKER, true)
                                putString(InlineDemoSession.EDITOR_FAMILY, family.name)
                            }
                        return connection
                    }
                }
                .apply {
                    id = R.id.inline_sample_input
                    hint = getString(R.string.inline_input_hint)
                    textSize = 16f
                    setAutofillHints(View.AUTOFILL_HINT_USERNAME)
                    importantForAutofill =
                        if (
                            getSystemService(AutofillManager::class.java)
                                .hasEnabledAutofillServices()
                        )
                            View.IMPORTANT_FOR_AUTOFILL_YES
                        else View.IMPORTANT_FOR_AUTOFILL_NO_EXCLUDE_DESCENDANTS
                }
        body.addView(inputWithExtras)
        fun requestWhenKeyboardIsReady() {
            if (!requestPending || !InlineDemoSession.keyboardReady.value) return
            requestPending = false
            val autofill = getSystemService(AutofillManager::class.java)
            // Replace any session started before the inline-capable keyboard was connected.
            autofill.cancel()
            autofill.requestAutofill(inputWithExtras)
        }
        action(R.string.inline_request_demo, R.id.inline_sample_request) {
            val ownedService =
                getSystemService(AutofillManager::class.java).hasEnabledAutofillServices()
            if (!ownedService) {
                requestPending = false
                inputWithExtras.importantForAutofill =
                    View.IMPORTANT_FOR_AUTOFILL_NO_EXCLUDE_DESCENDANTS
                status.setText(R.string.inline_status_empty)
                return@action
            }
            inputWithExtras.importantForAutofill = View.IMPORTANT_FOR_AUTOFILL_YES
            requestPending = true
            inputWithExtras.requestFocus()
            getSystemService(InputMethodManager::class.java)
                .showSoftInput(inputWithExtras, InputMethodManager.SHOW_IMPLICIT)
            requestWhenKeyboardIsReady()
        }
        action(R.string.hosted_sample_return, R.id.inline_sample_return) { finish() }
        val scroll = ScrollView(this).apply { addView(body) }
        ViewCompat.setOnApplyWindowInsetsListener(scroll) { view, insets ->
            val bars =
                insets.getInsets(
                    WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime()
                )
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }
        setContentView(scroll)
        ViewCompat.requestApplyInsets(scroll)
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    InlineDemoSession.keyboardReady.collect { ready ->
                        if (ready) requestWhenKeyboardIsReady()
                    }
                }
                InlineDemoSession.status.collect { sample ->
                    if (sample.phase == InlineDemoPhase.ATTACHED) inflated = true
                    status.text =
                        getString(
                            when (sample.phase) {
                                InlineDemoPhase.WAITING -> R.string.inline_status_waiting
                                InlineDemoPhase.EMPTY -> R.string.inline_status_empty
                                InlineDemoPhase.ATTACHED -> R.string.inline_status_ready
                                InlineDemoPhase.DETACHED -> R.string.inline_status_detached
                            }
                        ) +
                            if (sample.className.isEmpty()) ""
                            else "\n${sample.className} · ${sample.width} × ${sample.height}px"
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(EXTRA_INFLATED, inflated)
        super.onSaveInstanceState(outState)
    }

    override fun finish() {
        InlineDemoSession.keyboard.get()?.returnToPreviousKeyboard()
        setResult(RESULT_OK, Intent().putExtra(EXTRA_INFLATED, inflated))
        super.finish()
    }

    companion object {
        const val EXTRA_FAMILY = "platformFamily"
        const val EXTRA_INFLATED = "inflated"
    }
}
