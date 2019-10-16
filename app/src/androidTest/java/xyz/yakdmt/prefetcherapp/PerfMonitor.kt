package xyz.yakdmt.prefetcherapp

import android.app.Instrumentation
import android.os.Bundle
import android.os.ParcelFileDescriptor
import androidx.test.jank.IMonitor
import org.junit.Assert
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.util.*
import java.util.regex.Pattern
import kotlin.collections.ArrayList

internal class PerfMonitor(
    private val instrumentation: Instrumentation,
    private val process: String
) : IMonitor {

    private val stats = mutableMapOf<JankStat, Number>()

    // Patterns used for parsing dumpsys gfxinfo output
    enum class JankStat constructor(
        private val parsePattern: Pattern,
        private val groupIndex: Int,
        internal val type: Class<*>,
        optional: Boolean = false
    ) {
        TOTAL_FRAMES(Pattern.compile("\\s*Total frames rendered: (\\d+)"), 1, Int::class.java),
        NUM_JANKY(
            Pattern.compile("\\s*Janky frames: (\\d+) \\((\\d+(\\.\\d+))%\\)"), 2,
            Double::class.java
        ),
        FRAME_TIME_90TH(Pattern.compile("\\s*90th percentile: (\\d+)ms"), 1, Int::class.java),
        FRAME_TIME_95TH(Pattern.compile("\\s*95th percentile: (\\d+)ms"), 1, Int::class.java),
        FRAME_TIME_99TH(Pattern.compile("\\s*99th percentile: (\\d+)ms"), 1, Int::class.java);

        private var successfulParse = false
        internal var isOptional = false

        init {
            isOptional = optional
        }

        internal fun parse(line: String): String? {
            var ret: String? = null
            val matcher = parsePattern.matcher(line)
            if (matcher.matches()) {
                ret = matcher.group(groupIndex)
                successfulParse = true
            }
            return ret
        }

        internal fun wasParsedSuccessfully(): Boolean {
            return successfulParse
        }

        internal fun reset() {
            successfulParse = false
        }
    }

    @Throws(IOException::class)
    override fun startIteration() {
        // Clear out any previous data
        val stdout = executeShellCommand(
            String.format("dumpsys gfxinfo %s reset", process)
        )
        val bufferedReader = BufferedReader(InputStreamReader(stdout))

        bufferedReader.use { reader ->
            while (reader.readLine() != null) {
                // Read the output, but don't do anything with it
            }
        }
    }

    @Throws(IOException::class)
    override fun stopIteration(): Bundle {
        // Dump the latest stats
        val stdout = executeShellCommand(String.format("dumpsys gfxinfo %s", process))
        val bufferedReader = BufferedReader(InputStreamReader(stdout))

        // The frame stats section has the following output:
        // Total frames rendered: ###
        // Janky frames: ### (##.##%)
        // 50th percentile: ##ms
        // 90th percentile: ##ms
        // 95th percentile: ##ms
        // 99th percentile: ##ms

        bufferedReader.use { reader ->
            var line: String? = reader.readLine()
            do {
                // Attempt to parse the line as a frame stat value
                for (stat in JankStat.values()) {
                    val part: String? = stat.parse(line!!)
                    if (part != null) {
                        // Parse was successful. Add the numeric value to the accumulated list of
                        // values for that stat.
                        when {
                            stat.type == Int::class.java -> stats[stat] = Integer.valueOf(part)
                            stat.type == Double::class.java -> stats[stat] = java.lang.Double.valueOf(part)
                            else -> // Shouldn't get here
                                throw IllegalStateException("Unsupported JankStat type")
                        }
                        break
                    }
                }
                line = reader.readLine()
            } while (line != null)
        }

        // Make sure we found all the stats
        for (stat in JankStat.values()) {
            if (!stat.wasParsedSuccessfully() && !stat.isOptional) {
                Assert.fail(String.format("Failed to parse %s", stat.name))
            }
            stat.reset()
        }

        return metrics
    }

    private fun transformToPercentage(values: List<Int>, totals: List<Int>): List<Double> {
        val ret = ArrayList<Double>(values.size)

        val valuesItr = values.iterator()
        val totalsItr = totals.iterator()
        while (valuesItr.hasNext()) {
            val value = valuesItr.next().toDouble()
            val total = totalsItr.next().toDouble()

            ret.add(value / total * 100.0f)
        }

        return ret
    }

    private fun computeAverage(values: List<Int>): Int {
        var sum = 0

        for (value in values) {
            sum += value
        }

        return sum / values.size
    }

    override fun getMetrics(): Bundle {
        val metrics = Bundle()

        metrics.putInt(PerformanceTest.KEY_TOTAL_FRAMES, stats[JankStat.TOTAL_FRAMES] as Int)
        metrics.putDouble(PerformanceTest.KEY_NUM_JANKY, stats[JankStat.NUM_JANKY] as Double)
        metrics.putInt(PerformanceTest.KEY_FRAME_TIME_90TH_PERCENTILE, stats[JankStat.FRAME_TIME_90TH] as Int)
        metrics.putInt(PerformanceTest.KEY_FRAME_TIME_95TH_PERCENTILE, stats[JankStat.FRAME_TIME_95TH] as Int)
        metrics.putInt(PerformanceTest.KEY_FRAME_TIME_99TH_PERCENTILE, stats[JankStat.FRAME_TIME_99TH] as Int)

        return metrics
    }

    /**
     * Executes the given `command` as the shell user and returns an [InputStream]
     * containing the command's standard output.
     */
    private fun executeShellCommand(command: String): InputStream {
        val stdout = instrumentation.uiAutomation
            .executeShellCommand(command)
        return ParcelFileDescriptor.AutoCloseInputStream(stdout)
    }

}