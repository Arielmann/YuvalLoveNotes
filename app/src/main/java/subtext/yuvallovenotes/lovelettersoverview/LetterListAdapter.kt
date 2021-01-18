package subtext.yuvallovenotes.lovelettersoverview

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintSet
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import subtext.yuvallovenotes.R
import subtext.yuvallovenotes.crossapplication.listsadapter.ItemSelectionCallback
import subtext.yuvallovenotes.crossapplication.logic.adapter.DefaultDiffUtilCallback
import subtext.yuvallovenotes.crossapplication.models.loveitems.LoveLetter
import subtext.yuvallovenotes.databinding.VhLetterListBinding
import java.lang.ref.WeakReference
import java.util.*
import kotlin.collections.HashSet

/**
 * Adapter for managing the display of the [LoveLetter] list created by the user
 */
class LetterListAdapter(context: Context, val onLetterOpenRequest: (letter: LoveLetter) -> Unit, val lettersSelectionListener: ItemSelectionCallback) : ListAdapter<LoveLetter, LetterListAdapter.LetterListViewHolder>(DefaultDiffUtilCallback<LoveLetter>()) {

    companion object {
        val TAG: String = LetterListAdapter::class.simpleName!!
    }

    private val mBoundViewHolders: MutableSet<RecyclerView.ViewHolder> = HashSet()
    private var selectableItemsColor: Int = Color.WHITE
    var isSelectionModeActive: Boolean = false
    var selectedLetters: MutableList<LoveLetter> = mutableListOf()
    private var isRightToLeft: Boolean = context.resources.getBoolean(R.bool.is_right_to_left)
    private val weakContext: WeakReference<Context> = WeakReference(context)

    inner class LetterListViewHolder(private val binding: VhLetterListBinding) : RecyclerView.ViewHolder(binding.root) {

        init {
            itemView.setOnLongClickListener(View.OnLongClickListener {
                if (!isSelectionModeActive) {
                    val selectedPosition = bindingAdapterPosition
                    val selectedLetter = currentList[selectedPosition]
                    isSelectionModeActive = true
                    selectedLetters.add(selectedLetter)
                    lettersSelectionListener.onItemSelected()
                    binding.root.setBackgroundColor(Color.RED)
                    return@OnLongClickListener true
                }
                false
            })

            itemView.setOnClickListener {
                val selectedPosition = bindingAdapterPosition
                val selectedLetter = currentList[selectedPosition]

                //Capture Clicks in Selection Mode
                if (isSelectionModeActive) {
                    if (selectedLetters.contains(selectedLetter)) {
                        selectedLetters.remove(selectedLetter)
                        lettersSelectionListener.onItemRemoved()
                        binding.root.setBackgroundColor(Color.WHITE)
                    } else {
                        selectedLetters.add(selectedLetter)
                        lettersSelectionListener.onItemSelected()
                        binding.root.setBackgroundColor(Color.RED)
                    }
                }else{
                    onLetterOpenRequest.invoke(selectedLetter)
                }
            }
        }


        fun bind(letter: LoveLetter) {
            if (isSelectionModeActive) {
                if (selectedLetters.contains(letter)) {
                    binding.root.setBackgroundColor(Color.RED)
                } else {
                    binding.root.setBackgroundColor(Color.WHITE)
                }
            }

            binding.letterListViewHolderTextTV.text = letter.text
            if (isRightToLeft) {
                setViewHolderLayoutRightToLeft()
            }
            if (letter.isCreatedByUser) {
                binding.letterListViewHolderIconIV.setImageResource(R.drawable.ic_baseline_person_black_24)
            } else {
                binding.letterListViewHolderIconIV.setImageDrawable(null)
            }
        }

        private fun setViewHolderLayoutRightToLeft() {
            val constraintSet = ConstraintSet()
            constraintSet.clone(binding.letterListViewHolderCL)
            constraintSet.clear(R.id.letterListViewHolderIconIV, ConstraintSet.END)
            constraintSet.clear(R.id.letterListSelectionCheckbox, ConstraintSet.END)
            constraintSet.connect(R.id.letterListViewHolderIconIV, ConstraintSet.START, R.id.letterListSelectionCheckbox, ConstraintSet.END, 16)
            constraintSet.connect(R.id.letterListSelectionCheckbox, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, 8)
            constraintSet.clear(R.id.letterListViewHolderTextTV, ConstraintSet.START)
            constraintSet.connect(R.id.letterListViewHolderTextTV, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, 24)
            constraintSet.connect(R.id.letterListViewHolderTextTV, ConstraintSet.START, R.id.letterListSelectionCheckbox, ConstraintSet.END, 24)
            constraintSet.applyTo(binding.letterListViewHolderCL)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LetterListViewHolder {
        val binding = VhLetterListBinding.inflate(LayoutInflater.from(weakContext.get()), parent, false)
        return LetterListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LetterListViewHolder, position: Int) {
        mBoundViewHolders.add(holder)
        holder.bind(getItem(position))
    }

    override fun onViewRecycled(holder: LetterListViewHolder) {
        mBoundViewHolders.remove(holder)
    }

    override fun getItemId(position: Int): Long = position.toLong()

    fun selectAllLetters() {
        selectedLetters.addAll(currentList)
        for (holder in mBoundViewHolders) {
            holder.itemView.setBackgroundColor(Color.RED)
        }
    }

    fun clearSelectionMode() {
        isSelectionModeActive = false
        selectedLetters.clear()
        for (holder in mBoundViewHolders) {
            holder.itemView.setBackgroundColor(Color.WHITE)
        }
    }
}






