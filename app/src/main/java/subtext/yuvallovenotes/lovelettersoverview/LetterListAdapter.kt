package subtext.yuvallovenotes.lovelettersoverview

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
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

    private val weakContext: WeakReference<Context> = WeakReference(context)
    lateinit var onItemClickListener: (letter: LoveLetter) -> Unit

    inner class LetterListViewHolder(private val binding: VhLetterListBinding) : RecyclerView.ViewHolder(binding.root) {

        private lateinit var currentLetter: LoveLetter

        fun bind(letter: LoveLetter) {
            this.currentLetter = letter
            binding.letterListViewHolderTextTV.text = currentLetter.text
            binding.root.setOnClickListener{onItemClickListener.invoke(currentLetter)}
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


