package subtext.yuvallovenotes.lovelettersoverview

import android.content.Context
import android.util.Log.d
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintSet
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.vh_letter_list.view.*
import subtext.yuvallovenotes.R
import subtext.yuvallovenotes.crossapplication.listsadapter.DefaultDiffUtilCallback
import subtext.yuvallovenotes.crossapplication.listsadapter.ItemSelectionCallback
import subtext.yuvallovenotes.crossapplication.models.loveitems.LoveLetter
import subtext.yuvallovenotes.databinding.VhLetterListBinding
import java.lang.ref.WeakReference
import java.util.*
import kotlin.collections.HashSet

/**
 * Adapter for managing the display of the [LoveLetter] list created by the user
 */
class LetterListAdapter(context: Context, val onLetterOpenRequest: (letter: LoveLetter) -> Unit) : ListAdapter<LoveLetter, LetterListAdapter.LetterListViewHolder>(DefaultDiffUtilCallback<LoveLetter>()) {

    companion object {
        private val TAG: String = LetterListAdapter::class.simpleName!!
        private const val CHECKBOX_FADE_DURATION = 300L
    }

    private val boundViewHolders: MutableSet<LetterListViewHolder> = HashSet()
    var isSelectionModeActive: Boolean = false
    var selectedLetters: MutableSet<LoveLetter> = mutableSetOf()
    var favouriteLetters: MutableSet<LoveLetter> = mutableSetOf()
    lateinit var lettersSelectionListener: ItemSelectionCallback<LoveLetter>
    lateinit var favouriteLetterSelectionListener: ItemSelectionCallback<LoveLetter>
    private var isRightToLeft: Boolean = context.resources.getBoolean(R.bool.is_right_to_left)
    private val weakContext: WeakReference<Context> = WeakReference(context)

    inner class LetterListViewHolder(val binding: VhLetterListBinding) : RecyclerView.ViewHolder(binding.root) {

        init {
            itemView.setOnLongClickListener(View.OnLongClickListener {
                if (!isSelectionModeActive) {
                    val selectedPosition = bindingAdapterPosition
                    val selectedLetter = currentList[selectedPosition]
                    selectedLetters.add(selectedLetter)
                    lettersSelectionListener.onItemSelected(selectedLetter)
                    binding.letterListSelectionCheckbox.isChecked = true
                    boundViewHolders.forEach { holder ->
                        holder.binding.letterListSelectionCheckbox.animate().alpha(1f).setDuration(CHECKBOX_FADE_DURATION).start()
                        holder.binding.letterListFavouriteCheckbox.animate().alpha(0f).setDuration(CHECKBOX_FADE_DURATION).start()
                        holder.binding.favouriteLettersSupportBtn.visibility = View.GONE
                    }
                    isSelectionModeActive = true
                    return@OnLongClickListener true
                }
                false
            })

            itemView.setOnClickListener {
                val selectedPosition = bindingAdapterPosition
                val selectedLetter = currentList[selectedPosition]

                //Capture Clicks in Selection Mode
                if (isSelectionModeActive) {
                    binding.letterListSelectionCheckbox.performClick()
//                    onUserPressedSelectLetterCheckbox(selectedLetter)
                } else {
                    onLetterOpenRequest.invoke(selectedLetter)
                }
            }

            itemView.letterListSelectionCheckbox.setOnClickListener { view ->
                if (isSelectionModeActive) {
                    val selectedPosition = bindingAdapterPosition
                    val selectedLetter = currentList[selectedPosition]
                    onUserPressedSelectLetterCheckbox(selectedLetter)
                }
            }

            itemView.letterListFavouriteCheckbox.setOnClickListener { view ->
                val selectedPosition = bindingAdapterPosition
                val selectedLetter = currentList[selectedPosition]
                onUserPressedFavouriteLetterCheckbox(selectedLetter)
            }
        }

        /**
         * Called when user would like to add or remove a letter from the favourite letters list
         */
        private fun onUserPressedFavouriteLetterCheckbox(letter: LoveLetter) {
            if (favouriteLetters.contains(letter)) {
                letter.isFavourite = false
                favouriteLetterSelectionListener.itemWillBeRemovedFromSelectionList(letter)
                favouriteLetters.remove(letter)
                binding.letterListFavouriteCheckbox.isChecked = false
            } else {
                favouriteLetters.add(letter)
                letter.isFavourite = true
                favouriteLetterSelectionListener.onItemSelected(letter)
                binding.letterListFavouriteCheckbox.isChecked = true
            }
        }

        /**
         * Called when user would like to add or remove a letter from the selected letters list
         */
        private fun onUserPressedSelectLetterCheckbox(selectedLetter: LoveLetter) {
            if (selectedLetters.contains(selectedLetter)) {
                lettersSelectionListener.itemWillBeRemovedFromSelectionList(selectedLetter)
                selectedLetters.remove(selectedLetter)
                binding.letterListSelectionCheckbox.isChecked = false
            } else {
                selectedLetters.add(selectedLetter)
                lettersSelectionListener.onItemSelected(selectedLetter)
                binding.letterListSelectionCheckbox.isChecked = true
            }
        }


        fun bind(letter: LoveLetter) {
            if (isSelectionModeActive) {
                binding.letterListFavouriteCheckbox.alpha = 0f
                binding.letterListSelectionCheckbox.alpha = 1f
                binding.favouriteLettersSupportBtn.visibility = View.GONE
                binding.letterListFavouriteCheckbox.visibility = View.GONE
                binding.letterListSelectionCheckbox.isChecked = selectedLetters.contains(letter)
            }

            binding.letterListFavouriteCheckbox.isChecked = letter.isFavourite
            binding.favouriteLettersSupportBtn.setOnClickListener { binding.letterListFavouriteCheckbox.performClick() }

            binding.letterListViewHolderTextTV.text = letter.text
            if (isRightToLeft) {
                setViewHolderLayoutRightToLeft()
            }
            if (letter.isCreatedByUser) {
                binding.letterListViewHolderWrittenByUserIconIV.setImageResource(R.drawable.icon_letters_written_by_user_orange)
                binding.letterListViewHolderWrittenByUserIconIV.visibility = View.VISIBLE
            } else {
                binding.letterListViewHolderWrittenByUserIconIV.setImageDrawable(null)
                binding.letterListViewHolderWrittenByUserIconIV.visibility = View.GONE
            }
        }

        private fun setViewHolderLayoutRightToLeft() {
            val constraintSet = ConstraintSet()
            constraintSet.clone(binding.letterListViewHolderCL)
            constraintSet.clear(R.id.letterListViewHolderWrittenByUserIconIV, ConstraintSet.START)
            constraintSet.clear(R.id.letterListSelectionCheckbox, ConstraintSet.START)
            constraintSet.clear(R.id.letterListFavouriteCheckbox, ConstraintSet.START)
            constraintSet.clear(R.id.favouriteLettersSupportBtn, ConstraintSet.START)
            constraintSet.connect(R.id.letterListViewHolderWrittenByUserIconIV, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, 16)
            constraintSet.connect(R.id.letterListSelectionCheckbox, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, 8)
            constraintSet.connect(R.id.letterListFavouriteCheckbox, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, 8)
            constraintSet.connect(R.id.favouriteLettersSupportBtn, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, 8)
            constraintSet.clear(R.id.letterListViewHolderTextTV, ConstraintSet.START)
            constraintSet.connect(R.id.letterListViewHolderTextTV, ConstraintSet.END, R.id.letterListSelectionCheckbox, ConstraintSet.START, 24)
            constraintSet.connect(R.id.letterListViewHolderTextTV, ConstraintSet.START, R.id.letterListViewHolderWrittenByUserIconIV, ConstraintSet.END, 24)
            constraintSet.applyTo(binding.letterListViewHolderCL)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LetterListViewHolder {
        val binding = VhLetterListBinding.inflate(LayoutInflater.from(weakContext.get()), parent, false)
        return LetterListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LetterListViewHolder, position: Int) {
        boundViewHolders.add(holder)
        holder.bind(getItem(position))
    }

    override fun onViewRecycled(holder: LetterListViewHolder) {
        boundViewHolders.remove(holder)
    }

    override fun getItemId(position: Int): Long = position.toLong()

    fun selectAllLetters() {
        selectedLetters.addAll(currentList)
        for (holder in boundViewHolders) {
            holder.binding.letterListSelectionCheckbox.isChecked = true
        }
    }

    fun deselectAllLetters() {
        d(TAG, "Deselecting all letters")
        selectedLetters.clear()
        for (holder in boundViewHolders) {
            holder.binding.letterListSelectionCheckbox.isChecked = false
        }
    }

    fun exitSelectionMode() {
        d(TAG, "Exiting selection mode")
        isSelectionModeActive = false
        selectedLetters.clear()
        boundViewHolders.forEach { holder ->
            holder.binding.letterListFavouriteCheckbox.visibility = View.VISIBLE
            holder.binding.letterListFavouriteCheckbox.animate().alpha(1f).setDuration(CHECKBOX_FADE_DURATION).start()
            holder.binding.letterListSelectionCheckbox.animate().alpha(0f).setDuration(CHECKBOX_FADE_DURATION).start()
            holder.binding.letterListSelectionCheckbox.isChecked = false
            holder.binding.favouriteLettersSupportBtn.visibility = View.VISIBLE
        }
    }

    fun areAllItemsSelected(): Boolean {
        return currentList.size == selectedLetters.size
    }
}






