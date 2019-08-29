package xyz.yakdmt.prefetcherapp

import android.app.Activity
import android.os.Build
import androidx.test.jank.IMonitor
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ActivityTestRule
import junit.framework.TestCase
import org.junit.runner.Description
import org.junit.runners.model.Statement
import timber.log.Timber

open class ActivityPerfTestRule<T: Activity>(activityClass: Class<T>): ActivityTestRule<T>(activityClass) {

    private var monitor: IMonitor? = null
    private var annotation: PerformanceTest? = null

    init {
        if (API_LEVEL_ACTUAL <= 22) {
            error("Not supported by current platform.")
        }
    }

    override fun apply(base: Statement?, description: Description?): Statement {
        annotation = description?.getAnnotation(PerformanceTest::class.java)
        annotation?.let {
            monitor = PerfMonitor(InstrumentationRegistry.getInstrumentation(), it.processName)
        }
        return super.apply(base, description)
    }

    override fun beforeActivityLaunched() {
//        monitor?.startIteration()
        super.beforeActivityLaunched()
    }

    fun startIteration() {
        monitor?.startIteration()
    }

    override fun afterActivityFinished() {
        monitor?.let {
            val results = it.stopIteration()
            val res: Double = results?.get(annotation?.perfType?.type) as Double

            Timber.d("PREFETCHER IS ${enableImprovements} TOTAL = ${results.get(PerformanceTest.PerfType.TOTAL_FRAMES.type).toString().padEnd(5,' ')} AVG_NUM_JANKY = ${results.get(PerformanceTest.PerfType.AVG_NUM_JANKY.type).toString().padEnd(5, ' ')} AVG_FRAME_TIME_95TH = ${results.get(PerformanceTest.PerfType.AVG_FRAME_TIME_95TH.type).toString().padEnd(5, ' ')}")

            val assertion = when(annotation?.assertionType) {
                PerformanceTest.AssertionType.LESS -> res < annotation!!.threshold
                PerformanceTest.AssertionType.LESS_OR_EQUAL -> res <= annotation!!.threshold
                PerformanceTest.AssertionType.GREATER -> res > annotation!!.threshold
                PerformanceTest.AssertionType.GREATER_OR_EQUAL -> res >= annotation!!.threshold
                PerformanceTest.AssertionType.EQUAL -> res == annotation!!.threshold.toDouble()
                null -> false
            }
            TestCase.assertTrue(
                String.format(
                    "Monitor: %s, Expected: %d, Received: %f.",
                    annotation?.perfType?.type, annotation!!.threshold,
                    res
                ),
                assertion
            )
        }
        super.afterActivityFinished()
    }

    companion object {
        internal val API_LEVEL_ACTUAL =
            Build.VERSION.SDK_INT + if ("REL" == Build.VERSION.CODENAME) 0 else 1
    }
}