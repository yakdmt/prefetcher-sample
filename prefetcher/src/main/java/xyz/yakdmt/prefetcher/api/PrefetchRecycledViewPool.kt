package xyz.yakdmt.prefetcher.api

import android.app.Activity
import android.util.Log
import android.util.SparseArray
import android.util.SparseIntArray
import android.view.ViewGroup
import androidx.recyclerview.widget.*
import androidx.recyclerview.widget.attachToPreventViewPoolFromClearing
import androidx.recyclerview.widget.factorInCreateTime
import xyz.yakdmt.prefetcher.BuildConfig
import xyz.yakdmt.prefetcher.OffthreadViewPrefetcher
import xyz.yakdmt.prefetcher.RecyclerPrefetchingLogger
import kotlin.math.max

class PrefetchRecycledViewPool(private val activity: Activity) : RecyclerView.RecycledViewPool(), Prefetcher {
    private val prefetcher = OffthreadViewPrefetcher(activity, ::putViewFromPrefetcher)
    private val prefetchRegistry = if (BuildConfig.DEBUG) SparseIntArray() else null
    private val createdRegistry = if (BuildConfig.DEBUG) SparseIntArray() else null

    var listener: PrefetchedViewsCountListener? = null

    override fun setPrefetchedViewsCount(viewType: Int, count: Int, holderCreator: (fakeParent: ViewGroup, viewType: Int) -> RecyclerView.ViewHolder) {
        if (count <= 0) throw IllegalArgumentException("Prefetched count should be > 0")
        RecyclerPrefetchingLogger.log { "change views count: new=$count" }

        prefetcher.setPrefetchBound(holderCreator, viewType, count)

        prefetchRegistry?.put(viewType, max(prefetchRegistry.get(viewType), count))
    }

    fun start() {
        prefetcher.start()
        attachToPreventViewPoolFromClearing()
    }

    fun terminate() = prefetcher.terminate()

    override fun putRecycledView(scrap: RecyclerView.ViewHolder) {
        val viewType = scrap.itemViewType
        setMaxRecycledViews(viewType, 200)
        RecyclerPrefetchingLogger.log { "putRecycled view for viewType=${getViewTypeName(viewType)}" }

        super.putRecycledView(scrap)

        val recyclerView = scrap.nestedRecyclerView?.get()
        if (recyclerView is RecycleViewToParentViewPoolOnDetach) {
            val oldRecycledViewPool = recyclerView.recycledViewPool
            recyclerView.setRecycledViewPool(this)
            val recycler = recyclerView.recycler
            recyclerView.layoutManager?.removeAndRecycleAllViews(recycler)
            recycler.clear()
            recyclerView.setRecycledViewPool(oldRecycledViewPool)
        }
    }

    override fun getRecycledView(viewType: Int): RecyclerView.ViewHolder? {
        val holder = super.getRecycledView(viewType)
        if (holder == null) {
            prefetcher.itemCreatedOutside(viewType)
            createdRegistry?.let { it[viewType]++ }
            logPrefetchCountOverflow(viewType)
        } else {
            RecyclerPrefetchingLogger.log { "Holder with viewType=${getViewTypeName(viewType)}has been found" }
        }
        return holder
    }

    override fun clear() {
        RecyclerPrefetchingLogger.log { "Clear ViewPool" }
        prefetcher.clear()
        super.clear()
    }

    private fun putViewFromPrefetcher(scrap: RecyclerView.ViewHolder, creationTimeNs: Long) {
        factorInCreateTime(scrap.viewType, creationTimeNs)
        putRecycledView(scrap)
        createdRegistry?.let { it[scrap.viewType]++ }

        listener?.onViewCountChanged(calculatePrefetchedCount())
    }

    private fun calculatePrefetchedCount(): Int {
        var result = 0
        for (i in 0..10) {
            result += createdRegistry?.let { it[i] } ?: 0
        }
        return result
    }

    private fun logPrefetchCountOverflow(viewType: Int) {
        val created = createdRegistry?.get(viewType) ?: return
        val prefetch = prefetchRegistry!![viewType]
        if (created > prefetch) Log.w("PrefetchViewPool", "ViewPool cache miss: created=$created, prefetch=$prefetch, cached=${getRecycledViewCount(viewType)}, holder=${getViewTypeName(viewType)}")
    }

    //TODO
//    private val viewTypeNames by lazy { SparseArray<String>() }
//    private fun getViewTypeName(viewType: Int) = viewTypeNames.get(viewType) ?: activity.resources.getResourceName(viewType).also { viewTypeNames.put(viewType, it) }

    private fun getViewTypeName(viewType: Int) = viewType.toString()
}

operator fun SparseIntArray.set(key: Int, value: Int) = put(key, value)

interface PrefetchedViewsCountListener {
    fun onViewCountChanged(count: Int)
}
