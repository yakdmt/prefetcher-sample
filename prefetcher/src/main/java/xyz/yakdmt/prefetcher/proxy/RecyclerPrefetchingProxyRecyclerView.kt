@file:Suppress("PackageDirectoryMismatch")
package androidx.recyclerview.widget

import android.view.View

internal val ALLOW_THREAD_GAP_WORK = RecyclerView.ALLOW_THREAD_GAP_WORK

internal val RecyclerView.recycler
    get() = this.mRecycler

internal fun findNestedRecyclerView(view: View) = RecyclerView.findNestedRecyclerView(view)
