package subtext.yuvallovenotes.lovewritingtabs.ui.main.editor

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import subtext.yuvallovenotes.R
import subtext.yuvallovenotes.loveletters.LoveItem

class LoveItemVH(inflater: LayoutInflater, parent: ViewGroup):

    RecyclerView.ViewHolder(inflater.inflate(R.layout.love_item_edit_vh, parent, false)) {
        private var itemEditText: TextView? = null

        init {
            itemEditText = itemView.findViewById(R.id.loveItemEditorTv)
        }

        fun bind(item: LoveItem) {
            itemEditText?.text = item.text
        }
    }

fun <T : RecyclerView.ViewHolder> T.listen(event: (position: Int, type: Int) -> Unit): T {
    itemView.setOnClickListener {
        event.invoke(getAdapterPosition(), getItemViewType())
    }
    return this
}
