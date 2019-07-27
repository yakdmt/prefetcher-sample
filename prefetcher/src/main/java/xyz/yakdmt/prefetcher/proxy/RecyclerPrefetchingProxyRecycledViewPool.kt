@file:Suppress("PackageDirectoryMismatch")
package androidx.recyclerview.widget

import xyz.yakdmt.prefetcher.api.PrefetchRecycledViewPool

internal fun RecyclerView.RecycledViewPool.attachToPreventViewPoolFromClearing() {
    //It's necessary to call RecycledViewPool.attach() before any RecyclerView actual attaching
    //to prevent RecycledViewPool with already prefetched items from clearing on initial setAdapter() call.
    //Check RecyclerView.onAdapterChanged() for additional info.
    this.attach()
}

internal fun PrefetchRecycledViewPool.factorInCreateTime(viewType: Int, createTimeNs: Long) = (this as RecyclerView.RecycledViewPool).factorInCreateTime(viewType, createTimeNs)