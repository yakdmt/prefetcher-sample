package xyz.yakdmt.prefetcher

import android.app.Activity
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.os.Process
import android.util.Log
import android.util.SparseIntArray
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.*
import androidx.recyclerview.widget.ALLOW_THREAD_GAP_WORK
import androidx.recyclerview.widget.findNestedRecyclerView
import androidx.recyclerview.widget.viewType
import java.lang.ref.WeakReference

private const val TYPE_ITEMS_CREATED_OUTSIDE = 0
private const val TYPE_ENQUEUE_BATCH = 1
private const val TYPE_CREATE_ITEM = 2
private const val TYPE_CLEAR = 3

private const val URGENT_PRIORITY = 0L
private const val HIGH_PRIORITY = 1L
private const val MID_PRIORITY = 2L
private const val LOW_PRIORITY = 3L

private typealias HolderCreator = (fakeParent: ViewGroup, viewType: Int) -> RecyclerView.ViewHolder //fakeParent is necessary to allow parsing MarginLayoutParams from xml

internal class OffthreadViewPrefetcher(
        activity: Activity,
        private val holderConsumer: (holder: RecyclerView.ViewHolder, creationTimeNs: Long) -> Unit,
        private val thread: HandlerThread = HandlerThread("ViewPrefetcherThread", Process.THREAD_PRIORITY_BACKGROUND),
        private val mainThreadHandler: Handler = Handler(Looper.getMainLooper())
) {

    private val fakeParent by lazy { LinearLayout(activity) }

    private var terminated = false

    fun start(){
        when {
            terminated -> throw IllegalStateException("Prefetched already terminated")
            thread.state == Thread.State.NEW -> thread.start()
        }
    }

    fun clear() = handler.sendMessageAtTime(
            handler.obtainMessage(TYPE_CLEAR),
            URGENT_PRIORITY
    )

    fun terminate() {
        terminated = true
        thread.quit()
    }

    fun setPrefetchBound(holderCreator: HolderCreator, viewType: Int, count: Int) = handler.sendMessageAtTime(
            handler.obtainMessage(TYPE_ENQUEUE_BATCH, viewType, count, holderCreator),
            MID_PRIORITY
    )

    fun itemCreatedOutside(viewType: Int, count: Int = 1) = handler.sendMessageAtTime(
            handler.obtainMessage(TYPE_ITEMS_CREATED_OUTSIDE, viewType, count),
            HIGH_PRIORITY
    )

    private val itemsCreated = SparseIntArray()
    private val itemsQueued = SparseIntArray()

    private val handler by lazy {
        start()
        Handler(thread.looper) { msg ->
            @Suppress("UNCHECKED_CAST")
            when (msg.what) {
                TYPE_ITEMS_CREATED_OUTSIDE -> createdOutside(msg.arg1, msg.arg2)
                TYPE_ENQUEUE_BATCH -> enqueueBatch(msg.obj as HolderCreator, msg.arg1, msg.arg2)
                TYPE_CREATE_ITEM -> createItem(msg.obj as HolderCreator, msg.arg1)
                TYPE_CLEAR -> clearAndStop()
            }
            true
        }
    }

    private fun createdOutside(viewType: Int, count: Int) {
        itemsCreated[viewType] += count
        RecyclerPrefetchingLogger.log { "item created outside dequeue viewType=$viewType, created=${itemsCreated[viewType]}, queued=${itemsQueued[viewType]}" }
    }

    private fun enqueueBatch(holderCreator: HolderCreator, viewType: Int, count: Int) {
        if (itemsQueued[viewType] >= count) return
        itemsQueued[viewType] = count

        val created = itemsCreated[viewType]

        RecyclerPrefetchingLogger.log { "enqueueBatch viewType=$viewType, requestedCount=$count, created=$created" }

        if (created >= count) return

        enqueueItemCreation(holderCreator, viewType)
    }

    private fun createItem(holderCreator: HolderCreator, viewType: Int) {
        val created = itemsCreated[viewType] + 1
        val queued = itemsQueued[viewType]

        if (created > queued) return

        val holder: RecyclerView.ViewHolder
        val nestedRecyclerView: WeakReference<RecyclerView>?
        val start: Long
        val end: Long

        try {
            start = nanoTimeIfNeed()

            holder = holderCreator(fakeParent, viewType)

            nestedRecyclerView = if (ALLOW_THREAD_GAP_WORK) findNestedRecyclerView(holder.itemView)?.let { WeakReference(it) } else null

            end = nanoTimeIfNeed()
        } catch (e: Exception) {
            Log.e("PrefetchHandler", "Error while prefetching viewHolder for viewtype=$viewType", e)
            return
        }

        holder.viewType = viewType

        itemsCreated[viewType] = created

        RecyclerPrefetchingLogger.log { "prefetched viewType=$viewType, created=$created, queued=$queued" }

        mainThreadHandler.post {
            holder.nestedRecyclerView = nestedRecyclerView
            holderConsumer(holder, end - start)
        }

        if (created < queued) enqueueItemCreation(holderCreator, viewType)
    }

    private fun enqueueItemCreation(holderCreator: HolderCreator, viewType: Int) {
        handler.sendMessageAtTime(handler.obtainMessage(TYPE_CREATE_ITEM, viewType, 0, holderCreator), LOW_PRIORITY)
    }

    private fun clearAndStop() {
        handler.removeCallbacksAndMessages(null)
        itemsQueued.clear()
        itemsCreated.clear()
    }

    private fun nanoTimeIfNeed() = if (ALLOW_THREAD_GAP_WORK) System.nanoTime() else 0L
}
