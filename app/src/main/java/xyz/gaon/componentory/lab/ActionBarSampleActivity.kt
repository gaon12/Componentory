package xyz.gaon.componentory.lab

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.ViewTreeObserver
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import xyz.gaon.componentory.R
import xyz.gaon.componentory.settings.LanguagePreferences

class ActionBarSampleActivity : Activity() {
    private var clicks = 0
    private lateinit var status: TextView
    private val readableLayout =
        ViewTreeObserver.OnGlobalLayoutListener { window.decorView.ensureReadableText() }

    override fun attachBaseContext(newBase: Context) {
        val localized = LanguagePreferences.localizedContext(newBase)
        val light =
            Configuration(localized.resources.configuration).apply {
                uiMode =
                    (uiMode and Configuration.UI_MODE_NIGHT_MASK.inv()) or
                        Configuration.UI_MODE_NIGHT_NO
            }
        super.attachBaseContext(localized.createConfigurationContext(light))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val family =
            PlatformFamily.entries.firstOrNull { it.name == intent.getStringExtra(EXTRA_FAMILY) }
                ?: PlatformFamily.HOLO
        require(family != PlatformFamily.CLASSIC) { "Theme.Light does not supply an ActionBar." }
        setTheme(family.themeId)
        super.onCreate(savedInstanceState)
        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        clicks = savedInstanceState?.getInt("clicks") ?: intent.getIntExtra(EXTRA_CLICKS, 0)
        val bar = requireNotNull(actionBar)
        bar.setTitle(R.string.component_action_bar)
        bar.subtitle = family.themeName
        bar.setDisplayHomeAsUpEnabled(true)
        if (
            !(savedInstanceState?.getBoolean("visible")
                ?: intent.getBooleanExtra(EXTRA_VISIBLE, true))
        )
            bar.hide()

        val spacing = (20 * resources.displayMetrics.density).toInt()
        val body =
            LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(spacing, spacing, spacing, spacing)
            }
        body.addView(
            TextView(this).apply {
                setText(R.string.component_action_bar_description)
                textSize = 16f
            }
        )
        body.addView(
            TextView(this).apply {
                text = getString(R.string.runtime_sample_note, android.os.Build.VERSION.RELEASE)
                textSize = 16f
            }
        )
        body.addView(
            TextView(this).apply {
                text = "android.app.ActionBar\n${bar.javaClass.name}\nandroid:${family.themeName}"
                textSize = 16f
            }
        )
        status =
            TextView(this).apply {
                id = R.id.action_bar_sample_status
                textSize = 16f
            }
        body.addView(status)
        updateStatus()
        body.addView(
            Button(this).apply {
                id = R.id.action_bar_sample_toggle
                setText(R.string.action_bar_toggle)
                textSize = 16f
                setOnClickListener { if (bar.isShowing) bar.hide() else bar.show() }
            }
        )
        body.addView(
            Button(this).apply {
                id = R.id.action_bar_sample_reset
                setText(R.string.reset)
                textSize = 16f
                setOnClickListener {
                    clicks = 0
                    updateStatus()
                }
            }
        )
        body.addView(
            Button(this).apply {
                id = R.id.action_bar_sample_return
                setText(R.string.hosted_sample_return)
                textSize = 16f
                setOnClickListener { finish() }
            }
        )
        val scroll = ScrollView(this).apply { addView(body) }
        ViewCompat.setOnApplyWindowInsetsListener(scroll) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }
        setContentView(scroll)
        ViewCompat.requestApplyInsets(scroll)
        window.decorView.viewTreeObserver.addOnGlobalLayoutListener(readableLayout)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menu
            .add(Menu.NONE, R.id.action_bar_sample_action, Menu.NONE, R.string.option_a)
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            R.id.action_bar_sample_action -> {
                clicks++
                updateStatus()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt("clicks", clicks)
        outState.putBoolean("visible", actionBar?.isShowing == true)
        super.onSaveInstanceState(outState)
    }

    override fun finish() {
        setResult(
            RESULT_OK,
            Intent()
                .putExtra(EXTRA_CLICKS, clicks)
                .putExtra(EXTRA_VISIBLE, actionBar?.isShowing == true),
        )
        super.finish()
    }

    override fun onDestroy() {
        window.decorView.viewTreeObserver
            .takeIf { it.isAlive }
            ?.removeOnGlobalLayoutListener(readableLayout)
        super.onDestroy()
    }

    private fun updateStatus() {
        status.text = getString(R.string.status_clicks, clicks)
    }

    companion object {
        const val EXTRA_FAMILY = "platformFamily"
        const val EXTRA_CLICKS = "clicks"
        const val EXTRA_VISIBLE = "visible"
    }
}
