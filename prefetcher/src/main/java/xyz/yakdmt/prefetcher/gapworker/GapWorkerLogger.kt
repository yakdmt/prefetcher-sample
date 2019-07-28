package xyz.yakdmt.prefetcher.gapworker

import android.os.AsyncTask
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import timber.log.Timber

internal sealed class LogEvent
internal data class Task(val position: Int, val distanceToItem: Int, val viewVelocity: Int, val immediate: Boolean): LogEvent()
internal data class Attached(val view: RecyclerView, val position: Int): LogEvent()
internal data class Deadline(val remainingTime: Long?, val exiting: Boolean): LogEvent() {
    override fun toString() = "Deadline(remainingTime=${String.format("%,d", remainingTime)}, exiting=$exiting)"
}
internal class Attempt(val view: RecyclerView, val item: View? = null, val position: Int, val bound: Boolean = false): LogEvent() {
    override fun toString() = "Attempt(view=${view.javaClass.simpleName}, item=${item?.javaClass?.simpleName}, position=$position, bound=$bound)"
}
internal class Exit(
        private val frameTime: Long,
        private val frameWindow: Long,
        private val remainTime: Long,
        private val actualRemainTime: Long
): LogEvent() {
    override fun toString() = """
                Exit(
                    frameTime=        ${String.format("%,d", frameTime)}
                    frameWindow=      ${String.format("%,d", frameWindow)}
                    remainingTime=    ${String.format("%,d", remainTime)}
                    actualRemainTime= ${String.format("%,d", actualRemainTime)}
                )""".trimIndent()
}

@Suppress("NOTHING_TO_INLINE")
internal object GapWorkerLogger {
    private const val LOGGING_ENABLED = true

    internal val infos = mutableListOf<LogEvent>()
    internal var deadlineNs = 0L

    inline fun deadlineIs(deadlineNs: Long) {
        if (LOGGING_ENABLED) {
            GapWorkerLogger.deadlineNs = deadlineNs
        }
    }

    inline fun dump(fromIdleHandler: Boolean) {
        if (LOGGING_ENABLED) {
            val copy = infos.toList()
            AsyncTask.SERIAL_EXECUTOR.execute {
                Timber.tag("WrappedGapWorker").d("Go from ${"IdleHandler".takeIf { fromIdleHandler } ?: "RecyclerView"}:")
                copy.forEach { Timber.tag("WrappedGapWorker").d("  $it") }
            }
            infos.clear()
        }
    }

    inline fun attached(view: RecyclerView, position: Int) {
        if (LOGGING_ENABLED) {
            infos += Attached(view, position)
            infos += Deadline(deadlineNs - System.nanoTime(), false)
        }
    }

    inline fun attempt(view: RecyclerView, position: Int, itemView: View?, bound: Boolean) {
        if (LOGGING_ENABLED) {
            infos += Attempt(
                    view = view,
                    item = itemView,
                    position = position,
                    bound = bound
            )
            infos += Deadline(deadlineNs - System.nanoTime(), false)
        }
    }

    inline fun beforeExit(frameTime: Long, deadlineNs: Long, frameInterval: Long) {
        if (LOGGING_ENABLED) {
            val currentTime = System.nanoTime()
            infos += Exit(
                    frameTime,
                    deadlineNs - frameTime,
                    frameTime + frameInterval - currentTime,
                    deadlineNs - currentTime
            )
        }
    }

    inline fun deadlineChecked(remainTime: Long, exiting: Boolean) {
        if (LOGGING_ENABLED) {
            infos += Deadline(remainTime, exiting)
        }
    }

    inline fun startingTask(position: Int, distanceToItem: Int, viewVelocity: Int, immediate: Boolean) {
        if (LOGGING_ENABLED) {
            infos += Task(position, distanceToItem, viewVelocity, immediate)
        }
    }
}