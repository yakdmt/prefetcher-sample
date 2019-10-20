package xyz.yakdmt.prefetcher.api

import android.app.Activity
import android.util.Log
import android.util.SparseIntArray
import android.view.ViewGroup
import androidx.recyclerview.widget.*
import androidx.recyclerview.widget.attachToPreventViewPoolFromClearing
import androidx.recyclerview.widget.factorInCreateTime
import timber.log.Timber
import xyz.yakdmt.prefetcher.BuildConfig
import xyz.yakdmt.prefetcher.OffthreadViewCreator
import xyz.yakdmt.prefetcher.RecyclerPrefetchingLogger
import kotlin.math.max

class PrefetchRecycledViewPool(activity: Activity) : RecyclerView.RecycledViewPool(), Prefetcher {
    private val offthreadViewCreator = OffthreadViewCreator(activity, ::putViewFromCreator)
    private val prefetchRegistry = SparseIntArray()
    private val createdRegistry = SparseIntArray()

    var listener: PrefetchedViewsCountListener? = null

    override fun setPrefetchedViewsCount(viewType: Int, count: Int, holderFactory: (fakeParent: ViewGroup, viewType: Int) -> RecyclerView.ViewHolder) {
        require(count > 0) { "Prefetched count should be > 0" }
        RecyclerPrefetchingLogger.log { "change views count: new=$count" }

        offthreadViewCreator.setPrefetchBound(holderFactory, viewType, count)

        prefetchRegistry.put(viewType, max(prefetchRegistry.get(viewType), count))
    }

    fun start() {
        offthreadViewCreator.start()
        attachToPreventViewPoolFromClearing()
    }

    fun terminate() = offthreadViewCreator.terminate()

    override fun putRecycledView(scrap: RecyclerView.ViewHolder) {
        val viewType = scrap.itemViewType
        setMaxRecycledViews(viewType, 200) //200 - for educational purpose only. Choose it properly in real projects
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
            offthreadViewCreator.itemCreatedOutside(viewType)
            createdRegistry[viewType]++
            logUiThreadCreation(viewType)
        } else {
            RecyclerPrefetchingLogger.log { "Holder with viewType=${getViewTypeName(viewType)} has been found" }
        }
        return holder
    }

    override fun clear() {
        RecyclerPrefetchingLogger.log { "Clear ViewPool" }
        offthreadViewCreator.clear()
        super.clear()
    }

    private fun putViewFromCreator(scrap: RecyclerView.ViewHolder, creationTimeNs: Long) {
        factorInCreateTime(scrap.viewType, creationTimeNs)
        putRecycledView(scrap)
        createdRegistry[scrap.viewType]++

        listener?.onViewCountChanged(calculatePrefetchedCount())
    }

    private fun calculatePrefetchedCount(): Int {
        var result = 0
        for (i in 0..createdRegistry.size()) {
            result += createdRegistry[i]
        }
        return result
    }

    private fun logUiThreadCreation(viewType: Int) {
        val created = createdRegistry[viewType]
        val prefetch = prefetchRegistry[viewType]
        if (created > prefetch) Timber.w("ViewPool cache miss: created=$created, prefetch=$prefetch, cached=${getRecycledViewCount(viewType)}, holder=${getViewTypeName(viewType)}")
    }

    //TODO provide human-readable view type names
//    private val viewTypeNames by lazy { SparseArray<String>() }
//    private fun getViewTypeName(viewType: Int) = viewTypeNames.get(viewType) ?: activity.resources.getResourceName(viewType).also { viewTypeNames.put(viewType, it) }

    private fun getViewTypeName(viewType: Int) = viewType.toString()
}

operator fun SparseIntArray.set(key: Int, value: Int) = put(key, value)

interface PrefetchedViewsCountListener {
    fun onViewCountChanged(count: Int)
}
