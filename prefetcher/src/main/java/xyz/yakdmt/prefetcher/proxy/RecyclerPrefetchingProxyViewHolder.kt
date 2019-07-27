@file:Suppress("PackageDirectoryMismatch")
package androidx.recyclerview.widget

import java.lang.ref.WeakReference

internal var RecyclerView.ViewHolder.viewType: Int
    get() = itemViewType
    set(value) {
        mItemViewType = value
    }

internal var RecyclerView.ViewHolder.nestedRecyclerView: WeakReference<RecyclerView>?
    get() = mNestedRecyclerView
    set(value) {
        mNestedRecyclerView = value
    }
