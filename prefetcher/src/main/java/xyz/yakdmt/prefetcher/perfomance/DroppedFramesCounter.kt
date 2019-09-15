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
    private var lastTimestampNs = NEVER
    // Sentinel value indicating we have not yet received a frame callback since the observer was enabled
    // e.g. 0.01666 for a 60 Hz screen
    private var hardwareFrameIntervalSeconds: Double = 0.0

    var framesListener: DroppedFramesListener? = null

    private var droppedFrames = 0
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

    override fun doFrame(frameTimeNs: Long) {
        val frameIntervalNs = frameTimeNs - lastTimestampNs

        if (lastTimestampNs != NEVER) {
            val droppedFrameIntervalSeconds = hardwareFrameIntervalSeconds * 1.5

            val frameIntervalSeconds = frameIntervalNs / 1_000_000_000.0

            if (droppedFrameIntervalSeconds < frameIntervalSeconds) {
                droppedFrames += truncate(frameIntervalSeconds / hardwareFrameIntervalSeconds).toInt() - 1
                framesListener?.onFramesCounterChanged(droppedFrames)
            }
        }

        lastTimestampNs = frameTimeNs
        Choreographer.getInstance().postFrameCallback(this)
    }

    fun reset() {
        lastTimestampNs = NEVER
        droppedFrames = 0
    }

}

interface DroppedFramesListener {
    fun onFramesCounterChanged(count: Int)
}