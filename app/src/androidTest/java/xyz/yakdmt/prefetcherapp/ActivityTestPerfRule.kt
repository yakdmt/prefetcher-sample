package xyz.yakdmt.prefetcherapp

import android.app.Activity
import android.os.Build
import android.os.Bundle
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

            printMetricsToLogcat(results)

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

    private fun printMetricsToLogcat(results: Bundle) {
        val totalFrames = results.get(PerformanceTest.PerfType.TOTAL_FRAMES.type).toString().padEnd(5,' ')
        val jankyFrames = results.get(PerformanceTest.PerfType.NUM_JANKY.type).toString().padEnd(5, ' ')
        val percentile90 = results.get(PerformanceTest.PerfType.FRAME_TIME_90TH.type).toString().padEnd(3, ' ')
        val percentile95 = results.get(PerformanceTest.PerfType.FRAME_TIME_95TH.type).toString().padEnd(3, ' ')
        val percentile99 = results.get(PerformanceTest.PerfType.FRAME_TIME_99TH.type).toString().padEnd(3, ' ')
        Timber.d("IS_WITH_OPTS = $enableImprovements TOTAL = $totalFrames JANKY = $jankyFrames P_90TH = $percentile90 P_95TH = $percentile95 P_99TH = $percentile99")
    }

    companion object {
        internal val API_LEVEL_ACTUAL =
            Build.VERSION.SDK_INT + if ("REL" == Build.VERSION.CODENAME) 0 else 1
    }
}