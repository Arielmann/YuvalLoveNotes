package subtext.yuvallovenotes.lovewritingtabs.ui.main.editor

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import subtext.yuvallovenotes.R
import subtext.yuvallovenotes.loveletters.LoveItem

class LoveItemVH(inflater: LayoutInflater, parent: ViewGroup) :

    RecyclerView.ViewHolder(inflater.inflate(R.layout.list_item, parent, false)) {
        private var mTitleView: TextView? = null


        init {
            mTitleView = itemView.findViewById(R.id.list_title)
        }

        fun bind(item: LoveItem) {
            mTitleView?.text = item.text
        }

    }
