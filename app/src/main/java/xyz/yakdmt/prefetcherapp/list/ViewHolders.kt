package xyz.yakdmt.prefetcherapp.list

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import xyz.yakdmt.prefetcherapp.R
import java.lang.IllegalArgumentException

object HeavyViewHolderFactory {

    const val TYPE_RED = 0
    const val TYPE_ORANGE = 1
    const val TYPE_YELLOW = 2
    const val TYPE_GREEN = 3
    const val TYPE_BLUE = 4
    const val TYPE_INDIGO = 5
    const val TYPE_VIOLET = 6
    const val TYPE_BROWN = 7
    const val TYPE_FUCHSIA = 8
    const val TYPE_GOLD = 9

    fun createHolder(parent: ViewGroup, viewType: Int) : HeavyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item, parent, false)
        Thread.sleep(20)
        return when (viewType) {
            TYPE_RED -> RedViewHolder(view)
            TYPE_ORANGE -> OrangeViewHolder(view)
            TYPE_YELLOW -> YellowViewHolder(view)
            TYPE_GREEN -> GreenViewHolder(view)
            TYPE_BLUE -> BlueViewHolder(view)
            TYPE_INDIGO -> IndigoViewHolder(view)
            TYPE_VIOLET -> VioletViewHolder(view)
            TYPE_BROWN -> BrownViewHolder(view)
            TYPE_FUCHSIA -> FuchsiaViewHolder(view)
            TYPE_GOLD -> GoldViewHolder(view)
            else -> throw IllegalArgumentException("Unknown viewType")
        }
    }
}

abstract class HeavyViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    abstract val color: Int

    private val textView = view.findViewById<TextView>(R.id.text)

    fun bind(text: String) {
        textView.text = text
        itemView.setBackgroundColor(color)
    }
}

class RedViewHolder(view: View): HeavyViewHolder(view) {
    override val color: Int = view.context.getColor(R.color.holder_red)
}

class OrangeViewHolder(view: View): HeavyViewHolder(view) {
    override val color: Int = view.context.getColor(R.color.holder_orange)
}

class YellowViewHolder(view: View): HeavyViewHolder(view) {
    override val color: Int = view.context.getColor(R.color.holder_yellow)
}

class GreenViewHolder(view: View): HeavyViewHolder(view) {
    override val color: Int = view.context.getColor(R.color.holder_green)
}

class BlueViewHolder(view: View): HeavyViewHolder(view) {
    override val color: Int = view.context.getColor(R.color.holder_blue)
}

class IndigoViewHolder(view: View): HeavyViewHolder(view) {
    override val color: Int = view.context.getColor(R.color.holder_indigo)
}

class VioletViewHolder(view: View): HeavyViewHolder(view) {
    override val color: Int = view.context.getColor(R.color.holder_violet)
}

class BrownViewHolder(view: View): HeavyViewHolder(view) {
    override val color: Int = view.context.getColor(R.color.holder_brown)
}

class FuchsiaViewHolder(view: View): HeavyViewHolder(view) {
    override val color: Int = view.context.getColor(R.color.holder_fuchsia)
}

class GoldViewHolder(view: View): HeavyViewHolder(view) {
    override val color: Int = view.context.getColor(R.color.holder_gold)
}