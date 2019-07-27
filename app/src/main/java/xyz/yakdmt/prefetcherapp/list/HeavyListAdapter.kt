package xyz.yakdmt.prefetcherapp.list

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import xyz.yakdmt.prefetcherapp.models.HeavyModel

class HeavyListAdapter(private val items: List<HeavyModel>): RecyclerView.Adapter<HeavyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HeavyViewHolder {
        return HeavyViewHolderFactory.createHolder(parent, viewType)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: HeavyViewHolder, position: Int) {
        holder.bind(position.toString())
    }

    override fun getItemViewType(position: Int): Int {
        return (position / 50).coerceAtMost(9)
    }
}

