@file:Suppress("PackageDirectoryMismatch")
package androidx.recyclerview.widget

import android.view.View
import xyz.yakdmt.prefetcher.gapworker.GapWorkerIdleHandler
import xyz.yakdmt.prefetcher.gapworker.GapWorkerLogger
import java.util.concurrent.TimeUnit

private const val DEADLINE_EPSILON_NS = 0.3 * 1_000_000/*ms as ns*/

internal class GapWorkerWrapper(private val gapWorker: GapWorker = sGapWorker!!) : Runnable {

    private val tasksPool: GapWorkerTaskPool = GapWorkerTaskPool()
    private val idleHandler: GapWorkerIdleHandler = GapWorkerIdleHandler()

    init {
        idleHandler.callback = { prefetch(it, true) }
    }

    private var shouldCheckDeadline = false
    private var deadlineReachedSomewhere = false
    private var deadlineBeyondHope = false

    private var attachCounter = 0

    fun attach() {
        attachCounter++
    }

    fun detach() {
        attachCounter--
        if (attachCounter == 0) idleHandler.setEnabled(false)
    }

    override fun run() {
        gapWorker.mPostTimeNs = 0L
        if (gapWorker.mRecyclerViews.isEmpty()) return

        var latestFrameVsyncMs = 0L

        for (recyclerView in gapWorker.mRecyclerViews) {
            if (recyclerView.windowVisibility != View.VISIBLE) continue
            latestFrameVsyncMs = Math.max(recyclerView.drawingTime, latestFrameVsyncMs)
        }

        if (latestFrameVsyncMs == 0L) return

        prefetch(TimeUnit.MILLISECONDS.toNanos(latestFrameVsyncMs), false)
    }

    private fun prefetch(frameTime: Long, fromIdleHandler: Boolean) {
        shouldCheckDeadline = false
        deadlineReachedSomewhere = false
        deadlineBeyondHope = false

        val deadlineNs = frameTime + gapWorker.mFrameIntervalNs

        GapWorkerLogger.deadlineIs(deadlineNs)

        tasksPool.buildTaskList(gapWorker.mRecyclerViews)
        flushTasksWithDeadline(deadlineNs)

        GapWorkerLogger.beforeExit(frameTime, deadlineNs, gapWorker.mFrameIntervalNs)
        GapWorkerLogger.dump(fromIdleHandler)

        idleHandler.setEnabled(deadlineReachedSomewhere)
    }

    private fun flushTasksWithDeadline(deadlineNs: Long) {
        for (task in tasksPool) {
            if (task.view == null) return
            val taskDeadlineNs = if (task.immediate) Long.MAX_VALUE else deadlineNs

            GapWorkerLogger.startingTask(task.position, task.distanceToItem, task.viewVelocity, task.immediate)

            prefetchWithDeadline(task.view, task.position, taskDeadlineNs)

            task.clear()

            if (isDeadlineBeyondHope(taskDeadlineNs)) return
        }
    }

    private fun prefetchWithDeadline(view: RecyclerView, position: Int, deadlineNs: Long) {
        if (GapWorker.isPrefetchPositionAttached(view, position)) {
            GapWorkerLogger.attached(view, position)
            return
        }

        val holder = prefetchNotAttachedPositionWithDeadline(view, position, deadlineNs)

        if (isDeadlineBeyondHope(deadlineNs)) return

        if (holder != null && holder.isBound && !holder.isInvalid) {
            prefetchInnerRecyclerViewWithDeadline(holder.nestedRecycler ?: return, deadlineNs)
        }
    }

    private fun prefetchNotAttachedPositionWithDeadline(view: RecyclerView, position: Int, deadlineNs: Long): RecyclerView.ViewHolder? {
        val recycler = view.mRecycler

        var bound = false

        view.onEnterLayoutOrScroll()
        val holder = recycler.tryGetViewHolderForPositionByDeadline(position, false, deadlineNs)
        if (holder != null) {
            if (holder.isBound && !holder.isInvalid) {
                bound = true
                recycler.recycleView(holder.itemView)
            } else {
                recycler.addViewHolderToRecycledViewPool(holder, false)
            }
        }
        view.onExitLayoutOrScroll(false)

        shouldCheckDeadline = !bound

        GapWorkerLogger.attempt(view, position, holder?.itemView, bound)

        return holder
    }

    private fun prefetchInnerRecyclerViewWithDeadline(innerView: RecyclerView, deadlineNs: Long) {
        if (innerView.mDataSetHasChangedAfterLayout && innerView.mChildHelper.unfilteredChildCount != 0) {
            innerView.removeAndRecycleViews()
        }

        val innerPrefetchRegistry = innerView.mPrefetchRegistry
        innerPrefetchRegistry.collectPrefetchPositionsFromView(innerView, true)//todo try to resume
        if (innerPrefetchRegistry.mCount == 0) return

        innerView.mState.prepareForNestedPrefetch(innerView.mAdapter)

        val innerRecyclers = ArrayList<RecyclerView>(3)
        val attachedPositions = collectAttachedPositions(innerView)

        val allViewsCount = innerPrefetchRegistry.mCount * 2
        var i = 0
        while (i < allViewsCount) {
            val innerPosition = innerPrefetchRegistry.mPrefetchArray[i]
            i += 2

            if (attachedPositions.contains(innerPosition)) {
                GapWorkerLogger.attached(innerView, innerPosition)
                continue
            }

            val holder = prefetchNotAttachedPositionWithDeadline(innerView, innerPosition, deadlineNs)

            if (isDeadlineBeyondHope(deadlineNs)) return

            if (holder != null && holder.isBound && !holder.isInvalid) {
                holder.nestedRecycler?.let { innerRecyclers += it }
            }
        }

        for (recycler in innerRecyclers) {
            prefetchInnerRecyclerViewWithDeadline(recycler, deadlineNs)
            if (isDeadlineBeyondHope(deadlineNs)) return
        }
    }

    private fun collectAttachedPositions(view: RecyclerView): IntArray {
        val childCount = view.mChildHelper.unfilteredChildCount
        val attachedPositions = IntArray(childCount)

        for (i in 0 until childCount) {
            val attachedView = view.mChildHelper.getUnfilteredChildAt(i)
            val holder = RecyclerView.getChildViewHolderInt(attachedView)

            if (!holder.isInvalid) {
                attachedPositions[i] = holder.mPosition
            }
        }

        return attachedPositions
    }

    private fun isDeadlineBeyondHope(deadlineNs: Long): Boolean {
        if (deadlineBeyondHope) return true
        if (!shouldCheckDeadline) return false

        deadlineReachedSomewhere = true
        shouldCheckDeadline = false

        val remainingTime = deadlineNs - System.nanoTime()

        deadlineBeyondHope = remainingTime < DEADLINE_EPSILON_NS

        GapWorkerLogger.deadlineChecked(remainingTime, deadlineBeyondHope)

        return deadlineBeyondHope
    }
}

private val RecyclerView.ViewHolder?.nestedRecycler
    get() = this?.mNestedRecyclerView?.get()
