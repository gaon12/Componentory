package xyz.gaon.componentory.lab

import android.content.Intent
import android.view.View
import android.view.autofill.AutofillManager
import android.widget.EditText
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SdkSuppress
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assume.assumeFalse
import org.junit.Test
import org.junit.runner.RunWith
import xyz.gaon.componentory.R
import xyz.gaon.componentory.lab.inline.InlineDemoActivity

@RunWith(AndroidJUnit4::class)
@SdkSuppress(minSdkVersion = 30)
class InlineContentSafetyTest {
    @Test
    fun demoDoesNotRequestAutofillFromTheUsualProvider() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        assumeFalse(
            "This check needs the usual autofill provider rather than the demo provider.",
            context.getSystemService(AutofillManager::class.java).hasEnabledAutofillServices(),
        )
        ActivityScenario.launch<InlineDemoActivity>(Intent(context, InlineDemoActivity::class.java))
            .use { scenario ->
                scenario.onActivity { activity ->
                    val input = activity.findViewById<EditText>(R.id.inline_sample_input)
                    assertEquals(
                        View.IMPORTANT_FOR_AUTOFILL_NO_EXCLUDE_DESCENDANTS,
                        input.importantForAutofill,
                    )
                    activity.findViewById<View>(R.id.inline_sample_request).performClick()
                    assertEquals(
                        View.IMPORTANT_FOR_AUTOFILL_NO_EXCLUDE_DESCENDANTS,
                        input.importantForAutofill,
                    )
                    assertEquals("", input.text.toString())
                }
            }
    }
}
