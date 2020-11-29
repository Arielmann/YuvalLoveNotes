package subtext.yuvallovenotes.lovelettersoverview

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintSet
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import subtext.yuvallovenotes.R
import subtext.yuvallovenotes.crossapplication.logic.adapter.DefaultDiffUtilCallback
import subtext.yuvallovenotes.crossapplication.models.loveitems.LoveLetter
import subtext.yuvallovenotes.databinding.VhLetterListBinding
import java.lang.ref.WeakReference


/**
 * Adapter for managing the display of the [LoveLetter] list created by the user
 */
class LetterListAdapter(context: Context) : ListAdapter<LoveLetter, LetterListAdapter.LetterListViewHolder>(DefaultDiffUtilCallback<LoveLetter>()) {

    companion object {
        val TAG: String = LetterListAdapter::class.simpleName!!
    }

    private var isRightToLeft: Boolean = context.resources.getBoolean(R.bool.is_right_to_left)
    private val weakContext: WeakReference<Context> = WeakReference(context)
    lateinit var onItemClickListener: (letter: LoveLetter) -> Unit

    inner class LetterListViewHolder(private val binding: VhLetterListBinding) : RecyclerView.ViewHolder(binding.root) {

        private lateinit var currentLetter: LoveLetter

        fun bind(letter: LoveLetter) {
            this.currentLetter = letter
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
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LetterListViewHolder {
        val binding = VhLetterListBinding.inflate(LayoutInflater.from(weakContext.get()), parent, false)
        return LetterListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LetterListViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}


