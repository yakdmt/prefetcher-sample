package xyz.yakdmt.prefetcherapp.list

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_list.view.*
import xyz.yakdmt.prefetcher.api.PrefetchRecycledViewPool
import xyz.yakdmt.prefetcher.api.PrefetchedViewsCountListener
import xyz.yakdmt.prefetcher.api.Prefetcher
import xyz.yakdmt.prefetcher.gapworker.api.WrappedGapWorkerRecyclerView
import xyz.yakdmt.prefetcher.perfomance.DroppedFrameCounter
import xyz.yakdmt.prefetcherapp.R
import xyz.yakdmt.prefetcherapp.models.HeavyModelFactory

class HeavyListFragment : Fragment() {

    companion object {
        const val KEY_PREFETCHER_ENABLED = "prefetcher_enabled"
        const val KEY_GAPWORKER_ENABLED = "gapworker_enabled"
    }

    private lateinit var recyclerView: WrappedGapWorkerRecyclerView
    private lateinit var viewCounter: TextView
    private lateinit var progress: ProgressBar

    private lateinit var viewPool: PrefetchRecycledViewPool

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return View.inflate(context, R.layout.fragment_list, null)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.list)
        viewCounter = view.findViewById(R.id.view_counter)
        progress = view.findViewById(R.id.progress)

        recyclerView.adapter = HeavyListAdapter(HeavyModelFactory.create(500))
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.adapter?.notifyDataSetChanged()

        viewPool = PrefetchRecycledViewPool(activity as Activity)
        viewPool.listener = object : PrefetchedViewsCountListener {
            override fun onViewCountChanged(count: Int) {
                viewCounter.text = "Views prefetched: $count"
                progress.progress = count
            }
        }

        viewPool.start()
        recyclerView.setRecycledViewPool(viewPool)

        if (arguments?.getBoolean(KEY_PREFETCHER_ENABLED) == true) {
            prefetchItems(viewPool)
        }

        recyclerView.enableCustomGapworker = arguments?.getBoolean(KEY_GAPWORKER_ENABLED) ?: false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewPool.terminate()
    }

    private fun prefetchItems(prefetcher: Prefetcher) {
        val count = 20
        prefetcher.setPrefetchedViewsCount(HeavyViewHolderFactory.TYPE_ORANGE, count) { fakeParent, viewType ->
            HeavyViewHolderFactory.createHolder(fakeParent, viewType)
        }
        prefetcher.setPrefetchedViewsCount(HeavyViewHolderFactory.TYPE_YELLOW, count) { fakeParent, viewType ->
            HeavyViewHolderFactory.createHolder(fakeParent, viewType)
        }
        prefetcher.setPrefetchedViewsCount(HeavyViewHolderFactory.TYPE_GREEN, count) { fakeParent, viewType ->
            HeavyViewHolderFactory.createHolder(fakeParent, viewType)
        }
        prefetcher.setPrefetchedViewsCount(HeavyViewHolderFactory.TYPE_BLUE, count) { fakeParent, viewType ->
            HeavyViewHolderFactory.createHolder(fakeParent, viewType)
        }
        prefetcher.setPrefetchedViewsCount(HeavyViewHolderFactory.TYPE_INDIGO, count) { fakeParent, viewType ->
            HeavyViewHolderFactory.createHolder(fakeParent, viewType)
        }
        prefetcher.setPrefetchedViewsCount(HeavyViewHolderFactory.TYPE_VIOLET, count) { fakeParent, viewType ->
            HeavyViewHolderFactory.createHolder(fakeParent, viewType)
        }
        prefetcher.setPrefetchedViewsCount(HeavyViewHolderFactory.TYPE_BROWN, count) { fakeParent, viewType ->
            HeavyViewHolderFactory.createHolder(fakeParent, viewType)
        }
        prefetcher.setPrefetchedViewsCount(HeavyViewHolderFactory.TYPE_FUCHSIA, count) { fakeParent, viewType ->
            HeavyViewHolderFactory.createHolder(fakeParent, viewType)
        }
        prefetcher.setPrefetchedViewsCount(HeavyViewHolderFactory.TYPE_GOLD, count) { fakeParent, viewType ->
            HeavyViewHolderFactory.createHolder(fakeParent, viewType)
        }
        prefetcher.setPrefetchedViewsCount(HeavyViewHolderFactory.TYPE_RED, count) { fakeParent, viewType ->
            HeavyViewHolderFactory.createHolder(fakeParent, viewType)
        }
    }

}