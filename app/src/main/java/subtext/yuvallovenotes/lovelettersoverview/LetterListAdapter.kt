package subtext.yuvallovenotes.lovelettersoverview

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintSet
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import subtext.yuvallovenotes.R
import subtext.yuvallovenotes.crossapplication.logic.adapter.DefaultDiffUtilCallback
import subtext.yuvallovenotes.crossapplication.models.loveitems.LoveLetter
import subtext.yuvallovenotes.databinding.VhLetterListBinding
import java.lang.ref.WeakReference
import android.util.Log

/**
 * Adapter for managing the display of the [LoveLetter] list created by the user
 */
class LetterListAdapter(context: Context) : ListAdapter<LoveLetter, LetterListAdapter.LetterListViewHolder>(DefaultDiffUtilCallback<LoveLetter>()) {

    companion object {
        val TAG: String = LetterListAdapter::class.simpleName!!
    }

    var selectedLetters: MutableMap<Long, LoveLetter> = mutableMapOf()
    lateinit var lettersSelectionTracker: SelectionTracker<Long>
    private var isRightToLeft: Boolean = context.resources.getBoolean(R.bool.is_right_to_left)
    private val weakContext: WeakReference<Context> = WeakReference(context)
    lateinit var onItemClickListener: (letter: LoveLetter) -> Unit

    fun setSelectionsObserver() {
        lettersSelectionTracker.addObserver(object : SelectionTracker.SelectionObserver<Long>() {

            override fun onItemStateChanged(key: Long, selected: Boolean) {
                super.onItemStateChanged(key, selected)

                if (selected) {
                    Log.d(TAG, "Item $key is selected")
                    selectedLetters[key] = currentList[lettersSelectionTracker.selection.last().toInt()]
                } else {
                    selectedLetters.remove(key)
                    Log.d(TAG, "Item $key is deselected")
                }
            }
        })

    }

    inner class LetterListViewHolder(private val binding: VhLetterListBinding) : RecyclerView.ViewHolder(binding.root) {

        private lateinit var currentLetter: LoveLetter

        fun bind(letter: LoveLetter, isSelected: Boolean) {
            this.currentLetter = letter

            if (isSelected) {
                binding.root.setBackgroundColor(Color.RED)
            } else {
                binding.root.setBackgroundColor(Color.WHITE)
            }

            binding.letterListViewHolderTextTV.text = currentLetter.text
            if (currentLetter.isCreatedByUser) {

                if (isRightToLeft) {
                    setViewHolderLayoutRightToLeft()
                }
                binding.letterListViewHolderIconIV.setImageResource(R.drawable.ic_baseline_person_black_24)
            } else {
                binding.letterListViewHolderIconIV.setImageDrawable(null)
//                setViewHolderLayoutRightToLeft()
//                binding.letterListViewHolderIconIV.setImageResource(R.drawable.ic_baseline_person_black_24)
            }
            binding.root.setOnClickListener { onItemClickListener.invoke(currentLetter) }
        }

        private fun setViewHolderLayoutRightToLeft() {
            val constraintSet = ConstraintSet()
            constraintSet.clone(binding.letterListViewHolderCL)
            constraintSet.clear(R.id.letterListViewHolderIconIV, ConstraintSet.END)
            constraintSet.connect(R.id.letterListViewHolderIconIV, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, 8)
            constraintSet.clear(R.id.letterListViewHolderTextTV, ConstraintSet.START)
            constraintSet.connect(R.id.letterListViewHolderTextTV, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, 24)
            constraintSet.connect(R.id.letterListViewHolderTextTV, ConstraintSet.START, R.id.letterListViewHolderIconIV, ConstraintSet.END, 24)
            constraintSet.applyTo(binding.letterListViewHolderCL)
        }

        fun getItemDetails(): ItemDetailsLookup.ItemDetails<Long> = object : ItemDetailsLookup.ItemDetails<Long>() {
            override fun getPosition(): Int = bindingAdapterPosition
            override fun getSelectionKey(): Long = itemId
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LetterListViewHolder {
        val binding = VhLetterListBinding.inflate(LayoutInflater.from(weakContext.get()), parent, false)
        return LetterListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LetterListViewHolder, position: Int) {
        holder.bind(getItem(position), lettersSelectionTracker.isSelected(position.toLong()))
    }

    override fun getItemId(position: Int): Long = position.toLong()

    fun chooseAllLetters() {
        currentList.forEachIndexed { index, _ ->
            lettersSelectionTracker.select(index.toLong())
        }
    }

    class LetterListAdapterItemDetailsLookup(private val recyclerView: RecyclerView) : ItemDetailsLookup<Long>() {

        override fun getItemDetails(event: MotionEvent): ItemDetails<Long>? {
            val view = recyclerView.findChildViewUnder(event.x, event.y)
            if (view != null) {
                return (recyclerView.getChildViewHolder(view) as LetterListViewHolder).getItemDetails()
            }
            return null
        }
    }

}






