package xyz.yakdmt.prefetcher.gapworker

import android.os.Looper
import android.os.MessageQueue
import android.view.Choreographer

internal class GapWorkerIdleHandler(
        private val choreographer: Choreographer = Choreographer.getInstance(),
        private val loopersQueue: MessageQueue = Looper.myQueue()
) {
    var callback: ((frameTimeNs: Long) -> Unit)? = null

    private var enabled = false
    private var frameListenerRegistered = false


    private val listener = object : Choreographer.FrameCallback, MessageQueue.IdleHandler {
        private var idleListenerRegistered = false
        private var fromIdleCounter = 0

        private var choreographerFrameTime = 0L

        override fun doFrame(frameTimeNanos: Long) {
            if (!enabled) {
                frameListenerRegistered = false
                return
            }

            choreographerFrameTime = frameTimeNanos

            choreographer.postFrameCallback(this)

            if (!idleListenerRegistered) {
                idleListenerRegistered = true
                loopersQueue.addIdleHandler(this)
            }
        }

        override fun queueIdle(): Boolean {
            if (!enabled || fromIdleCounter++ > 10) {
                fromIdleCounter = 0
                idleListenerRegistered = false
                enabled = false //self disabling if we reach idle counter bound
                return false
            }

            callback!!(choreographerFrameTime)

            return true
        }
    }

    fun setEnabled(enabled: Boolean) {
        this.enabled = enabled
        if (enabled && !frameListenerRegistered) {
            frameListenerRegistered = true
            choreographer.postFrameCallback(listener)
        }
    }
}