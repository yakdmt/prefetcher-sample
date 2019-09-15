package xyz.yakdmt.prefetcherapp

import android.os.SystemClock
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice

import org.junit.Test

import org.junit.Before
import org.junit.Rule

class ScrollPerfTest {

    private lateinit var device: UiDevice

    @get:Rule
    var mainActivityActivityTestRule = ActivityPerfTestRule(MainActivity::class.java)

    @Before
    fun setup() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    }

    @Test
    @PerformanceTest(
        processName = PACKAGE_NAME,
        perfType = PerformanceTest.PerfType.AVG_NUM_JANKY,
        threshold = 1,
        assertionType = PerformanceTest.AssertionType.LESS_OR_EQUAL
    )
    fun testSecond() {
        enableImprovements = true
        if (enableImprovements) {
            onView(ViewMatchers.withId(R.id.use_prefetcher_switch))
                .perform(click())
            onView(ViewMatchers.withId(R.id.use_gapworker_switch))
                .perform(click())
        }

        onView(ViewMatchers.withId(R.id.go_to_list_button))
            .perform(click())

        SystemClock.sleep(5000)

        mainActivityActivityTestRule.startIteration()
        for (i in 0..10) {
            device.swipe(200, 1500, 500, 500, 5)
            SystemClock.sleep(500)
        }
    }

    companion object {
        private const val PACKAGE_NAME = "xyz.yakdmt.prefetcherapp"
    }
}
