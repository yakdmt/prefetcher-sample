@file:Suppress("PackageDirectoryMismatch")
package androidx.recyclerview.widget

internal val sGapWorker: GapWorker?
    get() = GapWorker.sGapWorker.get()

internal fun isGapWorker(runnable: Runnable) = runnable is GapWorker

