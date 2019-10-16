package xyz.yakdmt.prefetcherapp

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class PerformanceTest(
    val processName: String = "",
    val perfType: PerfType = PerfType.TOTAL_FRAMES,
    val threshold: Int = Int.MAX_VALUE,
    val assertionType: AssertionType = AssertionType.LESS_OR_EQUAL
) {
    enum class PerfType(val type: String) {
        TOTAL_FRAMES(KEY_TOTAL_FRAMES),
        NUM_JANKY(KEY_NUM_JANKY),
        FRAME_TIME_90TH(KEY_FRAME_TIME_90TH_PERCENTILE),
        FRAME_TIME_95TH(KEY_FRAME_TIME_95TH_PERCENTILE),
        FRAME_TIME_99TH(KEY_FRAME_TIME_99TH_PERCENTILE),
    }

    enum class AssertionType(val type: Int) {
        LESS(0),
        LESS_OR_EQUAL(1),
        GREATER(2),
        GREATER_OR_EQUAL(3),
        EQUAL(4)
    }

    companion object {
        const val KEY_TOTAL_FRAMES = "gfx-total-frames"
        const val KEY_NUM_JANKY = "gfx-jank"
        const val KEY_MAX_NUM_JANKY = "gfx-max-jank"
        const val KEY_FRAME_TIME_90TH_PERCENTILE = "gfx-frame-time-90"
        const val KEY_FRAME_TIME_95TH_PERCENTILE = "gfx-frame-time-95"
        const val KEY_FRAME_TIME_99TH_PERCENTILE = "gfx-frame-time-99"
    }
}