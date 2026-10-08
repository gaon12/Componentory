package xyz.gaon.componentory.eastereggs

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.PointF
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.os.SystemClock
import android.provider.Settings
import android.view.InputDevice
import android.view.MotionEvent
import android.view.View
import android.view.ViewTreeObserver
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.CompoundButton
import android.widget.FrameLayout
import android.widget.GridLayout
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.lifecycle.ActivityLifecycleCallback
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import androidx.test.runner.lifecycle.Stage
import com.android_m.egg.MLand
import com.android_p.egg.paint.Painting
import java.io.File
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import xyz.gaon.componentory.MainActivity
import xyz.gaon.componentory.eastereggs.port.R as EggR

// Current-OS runtime and input coverage; these are not original historical captures.
@RunWith(AndroidJUnit4::class)
class EasterEggGamesTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context = instrumentation.targetContext
    private val automation = instrumentation.uiAutomation
    private var originalAnimatorScale: String? = null
    private var preferences: Map<String, Map<String, *>> = emptyMap()
    private var components: Map<ComponentName, Int> = emptyMap()
    private lateinit var captures: File

    @Before
    fun preserveGameStateAndAllowLiveFrames() {
        originalAnimatorScale =
            Settings.Global.getString(
                context.contentResolver,
                Settings.Global.ANIMATOR_DURATION_SCALE,
            )
        require(originalAnimatorScale == null || originalAnimatorScale?.toFloatOrNull() != null)
        preferences =
            preferenceNames().associateWith {
                context.getSharedPreferences(it, Context.MODE_PRIVATE).all.mapValues { (_, value) ->
                    if (value is Set<*>) value.toSet() else value
                }
            }
        components =
            easterEggReleases
                .flatMap { release ->
                    listOf(release.logo.className) +
                        release.family.stages.map { it.className } +
                        release.family.integrations.map { it.className }
                }
                .map { ComponentName(context, it) }
                .distinct()
                .associateWith { context.packageManager.getComponentEnabledSetting(it) }
        captures =
            File(
                    checkNotNull(context.getExternalFilesDir("ui-tests")),
                    "eggs-${System.currentTimeMillis()}",
                )
                .apply { check(mkdirs()) }
        val componentSnapshot = File(captures, "component-state.json")
        componentSnapshot.writeText(
            JSONObject()
                .apply {
                    components.forEach { (name, value) -> put(name.flattenToString(), value) }
                }
                .toString()
        )
        report("component_snapshot", componentSnapshot.absolutePath)
        shell("settings put global animator_duration_scale 1.0")
    }

    @After
    fun restoreGameStateComponentsAndAnimationScale() {
        components.forEach { (name, value) ->
            context.packageManager.setComponentEnabledSetting(
                name,
                value,
                PackageManager.DONT_KILL_APP,
            )
        }
        (preferenceNames() + preferences.keys).forEach { name ->
            val editor = context.getSharedPreferences(name, Context.MODE_PRIVATE).edit().clear()
            preferences[name].orEmpty().forEach { (key, value) ->
                when (value) {
                    is String -> editor.putString(key, value)
                    is Boolean -> editor.putBoolean(key, value)
                    is Int -> editor.putInt(key, value)
                    is Long -> editor.putLong(key, value)
                    is Float -> editor.putFloat(key, value)
                    is Set<*> -> editor.putStringSet(key, value.map { it as String }.toSet())
                    else -> error("Unexpected preference type: $key")
                }
            }
            assertTrue("Restore $name", editor.commit())
        }
        shell(
            if (originalAnimatorScale == null) "settings delete global animator_duration_scale"
            else "settings put global animator_duration_scale $originalAnimatorScale"
        )
    }

    @Test
    fun everyCatalogLogoAndAdditionalScreenProducesAFrame() {
        val beanBagDream = ComponentName(context, "com.android_j.egg.BeanBagDream")
        // Exercise the first unlock even when a previous user session enabled the dream.
        context.packageManager.setComponentEnabledSetting(
            beanBagDream,
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP,
        )
        val screens =
            (easterEggReleases.map { it.logo } + easterEggReleases.flatMap { it.family.stages })
                .distinctBy { it.className }
                .filter {
                    Build.VERSION.SDK_INT >= it.minimumApi &&
                        it.className != "com.android_m.egg.preview.ShruggyActivity"
                }
        screens.forEach { stage ->
            withScreen(stage.className) { scenario ->
                // Several retained logos delay their entrance by 800–1000 ms.
                SystemClock.sleep(2_000)
                scenario.onActivity { assertFalse(stage.className, it.isFinishing) }
                if (stage.className == "com.android_j.egg.BeanBag") {
                    assertEquals(
                        PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                        context.packageManager.getComponentEnabledSetting(beanBagDream),
                    )
                }
                capture(stage.className)
            }
        }
        report("screen_count", screens.size.toString())
    }

    @Test
    fun nonogramTouchMarksSurviveActivityRecreation() {
        withScreen("com.android_q.egg.quares.QuaresActivity") { scenario ->
            var before = false
            lateinit var bounds: Rect
            scenario.onActivity {
                val cell = firstPuzzleCell(it)
                before = cell.isChecked
                bounds = visibleBounds(cell)
            }
            report("tap_rect", bounds.toShortString())
            capture("nonogram-before-touch")
            tap(bounds)
            scenario.onActivity { assertEquals(!before, firstPuzzleCell(it).isChecked) }
            scenario.recreate()
            scenario.onActivity { assertEquals(!before, firstPuzzleCell(it).isChecked) }
            capture("nonogram-restored-mark")
        }
    }

    @Test
    fun marshmallowPlayerButtonsChangeParticipantsBeforeTheGameStarts() {
        withScreen("com.android_m.egg.MLandActivity") { scenario ->
            var before = 0
            scenario.onActivity { before = it.findViewById<MLand>(EggR.id.world).numPlayers }
            tapView(scenario, EggR.id.player_plus_button)
            scenario.onActivity {
                assertEquals(before + 1, it.findViewById<MLand>(EggR.id.world).numPlayers)
            }
            tapView(scenario, EggR.id.player_minus_button)
            scenario.onActivity {
                assertEquals(before, it.findViewById<MLand>(EggR.id.world).numPlayers)
            }
            capture("marshmallow-player-controls")
        }
    }

    @Test
    fun pieDrawingChangesCanvasPixelsAndClearRemovesTheStroke() {
        withScreen("com.android_p.egg.paint.PaintActivity") { scenario ->
            lateinit var bounds: Rect
            var before = 0
            scenario.onActivity {
                val painting = painting(it)
                painting.zenMode = false
                bounds = visibleBounds(painting)
                before = painting.sampleAt(painting.width / 2f, painting.height / 2f)
            }
            val start = PointF(bounds.exactCenterX(), bounds.exactCenterY())
            gesture(start, PointF(start.x + 80f, start.y))
            scenario.onActivity {
                val painting = painting(it)
                assertNotEquals(
                    before,
                    painting.sampleAt(painting.width / 2f, painting.height / 2f),
                )
            }
            capture("pie-painted-stroke")
            tapView(scenario, EggR.id.btnClear)
            scenario.onActivity {
                val painting = painting(it)
                assertEquals(before, painting.sampleAt(painting.width / 2f, painting.height / 2f))
            }
        }
    }

    @Test
    fun allFourSpaceGamesRespondToFlightStickAndReleaseThrust() {
        listOf("u", "v", "baklava", "cinnamon_bun").forEach { code ->
            withScreen("com.android_$code.egg.landroid.MainActivity") { scenario ->
                waitFor("$code telemetry") { appText().contains("VEL:") }
                lateinit var bounds: Rect
                var density = 1f
                scenario.onActivity {
                    bounds = visibleBounds(it.window.decorView)
                    density = it.resources.displayMetrics.density
                }
                val start = PointF(bounds.exactCenterX(), bounds.exactCenterY())
                val end = PointF(start.x + 120f * density, start.y)
                val downTime = SystemClock.uptimeMillis()
                pointer(MotionEvent.ACTION_DOWN, start, downTime)
                try {
                    repeat(6) { index ->
                        pointer(
                            MotionEvent.ACTION_MOVE,
                            PointF(start.x + (end.x - start.x) * (index + 1) / 6f, start.y),
                            downTime,
                        )
                        SystemClock.sleep(30)
                    }
                    waitFor("$code nonzero thrust") {
                        Regex("THR: [1-9][0-9]*%").containsMatchIn(appText())
                    }
                    capture("space-$code-thrust")
                } finally {
                    pointer(MotionEvent.ACTION_UP, end, downTime)
                }
                waitFor("$code released thrust") {
                    val text = appText()
                    text.contains("VEL:") && !Regex("THR: [1-9][0-9]*%").containsMatchIn(text)
                }
            }
        }
    }

    @Test
    fun marshmallowPreviewShowsItsShrugToastAndClosesWithoutAWindow() {
        val name = "com.android_m.egg.preview.ShruggyActivity"
        val monitor = ActivityLifecycleMonitorRegistry.getInstance()
        val destroyed = CountDownLatch(1)
        val callback = ActivityLifecycleCallback { activity, stage ->
            if (activity.javaClass.name == name && stage == Stage.DESTROYED) destroyed.countDown()
        }
        val expected = context.getString(EggR.string.m_regrettable_lack_of_easter_egg)
        instrumentation.runOnMainSync { monitor.addLifecycleCallback(callback) }
        try {
            val event =
                automation.executeAndWaitForEvent(
                    {
                        instrumentation.runOnMainSync {
                            context.startActivity(
                                Intent(context, Class.forName(name))
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            )
                        }
                    },
                    {
                        it.eventType == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED &&
                            it.text.any { text -> text.toString().contains(expected) }
                    },
                    5_000,
                )
            assertTrue(event.text.any { it.toString().contains(expected) })
            assertTrue(
                "The preview toast activity should finish",
                destroyed.await(5, TimeUnit.SECONDS),
            )
            report("transient_action", name)
        } finally {
            instrumentation.runOnMainSync { monitor.removeLifecycleCallback(callback) }
        }
    }

    private fun withScreen(name: String, action: (GameScreen) -> Unit) {
        report("screen", name)
        val host =
            awaitResumed(MainActivity::class.java.name) {
                context.startActivity(
                    Intent(context, MainActivity::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
            }
        val hostScreen = GameScreen(host)
        var screen: GameScreen? = null
        try {
            val activity =
                awaitResumed(name) {
                    val stage =
                        (easterEggReleases.map { it.logo } +
                                easterEggReleases.flatMap { it.family.stages })
                            .first { it.className == name }
                    assertEquals(
                        "Launch $name through the catalog action",
                        null,
                        launchEgg(host, stage),
                    )
                }
            val game = GameScreen(activity)
            screen = game
            val drawn = CountDownLatch(1)
            val listener = ViewTreeObserver.OnDrawListener { drawn.countDown() }
            game.onActivity {
                it.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                it.window.decorView.viewTreeObserver.addOnDrawListener(listener)
                it.window.decorView.invalidate()
            }
            assertTrue("$name did not draw", drawn.await(5, TimeUnit.SECONDS))
            game.onActivity { it.window.decorView.viewTreeObserver.removeOnDrawListener(listener) }
            waitFor("$name focused window") {
                var focused = false
                game.onActivity {
                    focused =
                        it.window.decorView.hasWindowFocus() &&
                            it.window.decorView.width > 0 &&
                            it.window.decorView.height > 0
                }
                focused
            }
            action(game)
        } finally {
            try {
                screen?.close()
            } finally {
                hostScreen.close()
            }
        }
    }

    // ActivityScenario waits for an idle main queue; these games redraw continuously.
    private fun awaitResumed(
        name: String,
        previous: Activity? = null,
        start: () -> Unit,
    ): Activity {
        val monitor = ActivityLifecycleMonitorRegistry.getInstance()
        val resumed = CountDownLatch(1)
        var result: Activity? = null
        val callback = ActivityLifecycleCallback { activity, stage ->
            if (
                activity.javaClass.name == name && activity !== previous && stage == Stage.RESUMED
            ) {
                result = activity
                resumed.countDown()
            }
        }
        try {
            instrumentation.runOnMainSync {
                monitor.addLifecycleCallback(callback)
                start()
            }
            assertTrue("$name did not resume", resumed.await(10, TimeUnit.SECONDS))
            return checkNotNull(result)
        } finally {
            instrumentation.runOnMainSync { monitor.removeLifecycleCallback(callback) }
        }
    }

    private inner class GameScreen(private var activity: Activity) {
        fun onActivity(action: (Activity) -> Unit) =
            instrumentation.runOnMainSync { action(activity) }

        fun recreate() {
            val previous = activity
            activity = awaitResumed(previous.javaClass.name, previous) { previous.recreate() }
        }

        fun close() {
            val monitor = ActivityLifecycleMonitorRegistry.getInstance()
            val destroyed = CountDownLatch(1)
            val callback = ActivityLifecycleCallback { target, stage ->
                if (target === activity && stage == Stage.DESTROYED) destroyed.countDown()
            }
            try {
                instrumentation.runOnMainSync {
                    monitor.addLifecycleCallback(callback)
                    if (activity.isDestroyed) destroyed.countDown() else activity.finish()
                }
                assertTrue(
                    "${activity.javaClass.name} did not close",
                    destroyed.await(10, TimeUnit.SECONDS),
                )
            } finally {
                instrumentation.runOnMainSync { monitor.removeLifecycleCallback(callback) }
            }
        }
    }

    private fun firstPuzzleCell(activity: Activity): CompoundButton {
        val grid = activity.findViewById<GridLayout>(EggR.id.grid)
        assertTrue(grid.columnCount > 1 && grid.rowCount > 1)
        return grid.getChildAt(grid.columnCount + 1) as CompoundButton
    }

    private fun visibleBounds(view: View): Rect {
        val visible = Rect()
        assertTrue(
            "${view.javaClass.simpleName} must be visible",
            view.getGlobalVisibleRect(visible),
        )
        assertTrue(visible.width() > 0 && visible.height() > 0)
        val location = IntArray(2)
        view.getLocationOnScreen(location)
        return Rect(location[0], location[1], location[0] + view.width, location[1] + view.height)
    }

    private fun painting(activity: Activity): Painting {
        val container = activity.findViewById<FrameLayout>(EggR.id.contentView)
        return (0 until container.childCount)
            .map { container.getChildAt(it) }
            .filterIsInstance<Painting>()
            .single()
    }

    private fun tapView(scenario: GameScreen, id: Int) {
        lateinit var bounds: Rect
        scenario.onActivity { bounds = visibleBounds(it.findViewById(id)) }
        tap(bounds)
    }

    private fun tap(bounds: Rect) =
        gesture(
            PointF(bounds.exactCenterX(), bounds.exactCenterY()),
            PointF(bounds.exactCenterX(), bounds.exactCenterY()),
        )

    private fun gesture(start: PointF, end: PointF) {
        val downTime = SystemClock.uptimeMillis()
        pointer(MotionEvent.ACTION_DOWN, start, downTime)
        try {
            repeat(8) { index ->
                pointer(
                    MotionEvent.ACTION_MOVE,
                    PointF(
                        start.x + (end.x - start.x) * (index + 1) / 8f,
                        start.y + (end.y - start.y) * (index + 1) / 8f,
                    ),
                    downTime,
                )
                SystemClock.sleep(20)
            }
        } finally {
            pointer(MotionEvent.ACTION_UP, end, downTime)
        }
    }

    private fun pointer(action: Int, point: PointF, downTime: Long) {
        val event =
            MotionEvent.obtain(downTime, SystemClock.uptimeMillis(), action, point.x, point.y, 0)
                .apply { source = InputDevice.SOURCE_TOUCHSCREEN }
        try {
            assertTrue("Touch input must be accepted", automation.injectInputEvent(event, true))
        } finally {
            event.recycle()
        }
    }

    private fun appText(): String {
        fun text(node: AccessibilityNodeInfo): String = buildString {
            node.text?.let {
                append(it)
                append('\n')
            }
            for (index in 0 until node.childCount) node.getChild(index)?.let { append(text(it)) }
        }
        return automation.rootInActiveWindow
            ?.takeIf { it.packageName == context.packageName }
            ?.let(::text)
            .orEmpty()
    }

    private fun waitFor(message: String, condition: () -> Boolean) {
        val deadline = SystemClock.uptimeMillis() + 5_000
        while (SystemClock.uptimeMillis() < deadline) {
            if (condition()) return
            SystemClock.sleep(50)
        }
        assertTrue(message, condition())
    }

    private fun capture(name: String) {
        val bitmap = checkNotNull(automation.takeScreenshot())
        val file = File(captures, name.replace('.', '_') + ".png")
        try {
            file.outputStream().use {
                assertTrue(bitmap.compress(Bitmap.CompressFormat.PNG, 100, it))
            }
        } finally {
            bitmap.recycle()
        }
        report("capture", file.absolutePath)
    }

    private fun report(key: String, value: String) =
        instrumentation.sendStatus(0, Bundle().apply { putString("egg_$key", value) })

    private fun preferenceNames(): Set<String> =
        File(context.applicationInfo.dataDir, "shared_prefs")
            .listFiles()
            .orEmpty()
            .filter { it.extension == "xml" }
            .map { it.nameWithoutExtension }
            .toSet()

    private fun shell(command: String) =
        ParcelFileDescriptor.AutoCloseInputStream(automation.executeShellCommand(command)).use {
            it.readBytes()
            Unit
        }
}
