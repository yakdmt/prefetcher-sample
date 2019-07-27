package xyz.yakdmt.prefetcherapp.list

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import xyz.yakdmt.prefetcherapp.R
import xyz.yakdmt.prefetcherapp.models.HeavyModel

class HeavyListAdapter(private val items: List<HeavyModel>): RecyclerView.Adapter<HeavyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HeavyViewHolder {
        return HeavyViewHolder(View.inflate(parent.context, R.layout.list_item, null))
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: HeavyViewHolder, position: Int) {
        holder.bind(position.toString())
    }
}

class HeavyViewHolder(view: View): RecyclerView.ViewHolder(view) {

    private val textView = view.findViewById<TextView>(R.id.text)

    fun bind(text: String) {
        textView.text = text
    }
}