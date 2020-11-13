package subtext.yuvallovenotes.lovetabs.ui.main.editor

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import subtext.yuvallovenotes.loveitems.LoveItem

class ListAdapter(private val list: MutableList<LoveItem>) : RecyclerView.Adapter<LoveItemVH>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LoveItemVH {
        val inflater = LayoutInflater.from(parent.context)
        return LoveItemVH(inflater, parent).listen { pos, type ->
            val item = list.get(pos)

        }
    }

    override fun onBindViewHolder(holder: LoveItemVH, position: Int) {
        val loveItem: LoveItem = list[position]
        holder.bind(loveItem)
    }

    override fun getItemCount(): Int = list.size

}