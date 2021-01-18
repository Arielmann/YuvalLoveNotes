package subtext.yuvallovenotes.lovelettersoverview

import subtext.yuvallovenotes.crossapplication.models.loveitems.LoveLetter
import androidx.recyclerview.widget.RecyclerView
import subtext.yuvallovenotes.lovelettersoverview.ItemAdapter.ItemHolder
import subtext.yuvallovenotes.crossapplication.listsadapter.ItemSelectionCallback
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import subtext.yuvallovenotes.R
import android.view.View.OnLongClickListener
import android.widget.ImageView
import java.util.ArrayList
import java.util.HashSet

class ItemAdapter(Items: ArrayList<LoveLetter>?) : RecyclerView.Adapter<ItemHolder>(), ItemSelectionCallback {
    var selectionMode = false
    var selectedItems: HashSet<LoveLetter> = HashSet()
    var mItems: ArrayList<LoveLetter>? = Items

    fun enterSelectionModeWithItem(selectedItemPosition: Int) {
        if (selectedItemPosition >= 0 && selectedItemPosition < mItems!!.size) selectedItems.add(mItems!![selectedItemPosition])
        selectionMode = true
        notifyDataSetChanged()
    }

    fun clearSelectionMode() {
        selectionMode = false
        selectedItems.clear()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.vh_letter_list, parent, false)
        return ItemHolder(view)
    }

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        holder.setupView(mItems!![position])
    }

    override fun getItemCount(): Int {
        return if (mItems != null) mItems!!.size else 0
    }

    override fun onItemSelected() {}
    override fun onItemRemoved() {

    }

    inner class ItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var mImage: ImageView = itemView.findViewById(R.id.image)

        fun setupView(item: LoveLetter) {
            if (selectionMode) {
                if (selectedItems.contains(item)) {
                    mImage.setImageResource(R.drawable.lb_selectable_item_rounded_rect)
                } else {
                    mImage.setImageResource(R.drawable.ic_delete_sweep_white_36dp)
                }
            }
        }

        init {
            itemView.setOnLongClickListener(OnLongClickListener {
                if (!selectionMode) {
                    val selectedPosition = bindingAdapterPosition
                    val selectedItem = mItems!![selectedPosition]
                    enterSelectionModeWithItem(selectedPosition)
                    return@OnLongClickListener true
                }
                false
            })

            itemView.setOnClickListener {
                val selectedPosition = bindingAdapterPosition
                val selectedItem = mItems!![selectedPosition]

                //Capture Clicks in Selection Mode
                if (selectionMode) {
                    if (selectedItems.contains(selectedItem)) {
                        selectedItems.remove(selectedItem)
                        mImage.setImageResource(R.drawable.love_letter_image)
                    } else {
                        selectedItems.add(selectedItem)
                        mImage.setImageResource(R.drawable.lovers_logo)
                    }
                }
            }
        }
    }

}