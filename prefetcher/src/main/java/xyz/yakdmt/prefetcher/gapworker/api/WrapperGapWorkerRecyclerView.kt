package xyz.yakdmt.prefetcher.gapworker.api

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.GapWorkerWrapper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.isGapWorker
import androidx.recyclerview.widget.ALLOW_THREAD_GAP_WORK

class WrappedGapWorkerRecyclerView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : RecyclerView(context, attrs, defStyleAttr) {

    companion object {
        private val customGapWorker by lazy { if (ALLOW_THREAD_GAP_WORK) GapWorkerWrapper() else null }
    }

    var enableCustomGapworker = false

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        customGapWorker?.attach()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        customGapWorker?.detach()
    }

    override fun post(runnable: Runnable): Boolean {
        val shouldGetOriginalRunnable = !isGapWorker(runnable) || customGapWorker == null || !enableCustomGapworker
        val customRunnable = runnable.takeIf { shouldGetOriginalRunnable } ?: customGapWorker
        return super.post(customRunnable)
    }
}