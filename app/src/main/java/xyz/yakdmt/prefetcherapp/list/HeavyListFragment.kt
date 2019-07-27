package xyz.yakdmt.prefetcherapp.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import xyz.yakdmt.prefetcherapp.R
import xyz.yakdmt.prefetcherapp.models.HeavyModelFactory

class HeavyListFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return View.inflate(context, R.layout.fragment_list, null)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.list)

        recyclerView.adapter = HeavyListAdapter(HeavyModelFactory.create(1000))
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.adapter?.notifyDataSetChanged()
    }

}