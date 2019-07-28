package xyz.yakdmt.prefetcher.perfomance

import android.content.Context
import android.view.Choreographer
import android.view.WindowManager
import kotlin.math.truncate

class DroppedFrameCounter(context: Context) : Choreographer.FrameCallback {

    companion object {
        private val NEVER: Long = -1
    }
    // Last timestamp received from a frame callback, used to measure the next frame interval
    private var lastTimestampNanoseconds = NEVER
    // Sentinel value indicating we have not yet received a frame callback since the observer was enabled
    // e.g. 0.01666 for a 60 Hz screen
    private var hardwareFrameIntervalSeconds: Double = 0.0

    var framesListener: DroppedFramesListener? = null

    var droppedFrames = 0
        private set

    init {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = windowManager.defaultDisplay
        val hardwareFramesPerSecond = display.refreshRate

        hardwareFrameIntervalSeconds = 1.0 / hardwareFramesPerSecond

        addToChoreographer()
    }

    private fun addToChoreographer() {
        val choreographer = Choreographer.getInstance()
        choreographer.removeFrameCallback(this)
        choreographer.postFrameCallback(this)
    }

    override fun doFrame(frameTimeNanos: Long) {
        val frameIntervalNanoseconds = frameTimeNanos - lastTimestampNanoseconds

        // To detect a dropped frame, we need to know the interval between two frame callbacks.
        // If this is the first, wait for the second.
        if (lastTimestampNanoseconds != NEVER) {
            // With no dropped frames, frame intervals will roughly equal the hardware interval.
            // 2x the hardware interval means we definitely dropped one frame.
            // So our measuring stick is 1.5x.
            val droppedFrameIntervalSeconds = hardwareFrameIntervalSeconds * 1.5

            val frameIntervalSeconds = frameIntervalNanoseconds / 1_000_000_000.0

            if (droppedFrameIntervalSeconds < frameIntervalSeconds) {
                droppedFrames += truncate(frameIntervalSeconds / hardwareFrameIntervalSeconds).toInt() - 1
                framesListener?.onFramesCounterChanged(droppedFrames)
            }
        }

        lastTimestampNanoseconds = frameTimeNanos
        Choreographer.getInstance().postFrameCallback(this)
    }

    fun reset() {
        lastTimestampNanoseconds = NEVER
        droppedFrames = 0
    }

}

interface DroppedFramesListener {
    fun onFramesCounterChanged(count: Int)
}