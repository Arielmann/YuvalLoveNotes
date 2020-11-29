package subtext.yuvallovenotes.lovelettersoverview

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import org.koin.android.ext.android.get
import subtext.yuvallovenotes.R
import subtext.yuvallovenotes.crossapplication.models.loveitems.LoveLetter
import subtext.yuvallovenotes.crossapplication.utils.LoveUtils
import subtext.yuvallovenotes.databinding.FragmentLetterListBinding
import subtext.yuvallovenotes.crossapplication.viewmodel.LoveItemsViewModel


class LetterListFragment : Fragment() {

    companion object {
        private val TAG = LetterListFragment::class.simpleName
    }

    private lateinit var binding: FragmentLetterListBinding
    private val loveItemsViewModel: LoveItemsViewModel = get()
    private lateinit var lettersListAdapter: LetterListAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentLetterListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViewModel()
        setupLetterList()
        setOnClickListeners()
        LoveUtils.setupFragmentDefaultToolbar(this, binding.letterListToolBar)
    }

    private fun setupViewModel() {
        observeDataUpdates()
    }

    private fun observeDataUpdates() {
        loveItemsViewModel.loveLetters.observe(viewLifecycleOwner) { letters ->
            // Update the cached copy of the letters in the adapter.
            letters?.let { letters ->
                Log.d(TAG, "Updating letters list UI. letters: {$letters}")
                lettersListAdapter.submitList(letters.filter{ !it.isDisabled }.sortedBy { !it.isCreatedByUser })
            }
        }
    }

    private fun setOnClickListeners() {
        binding.createLetterBtn.setOnClickListener {
            val newLetter = LoveLetter()
            newLetter.isCreatedByUser = true
            loveItemsViewModel.insertLetter(newLetter)
            val action = LetterListFragmentDirections.navigateToLetterGenerator(newLetter.id)
            findNavController().navigate(action)
        }
    }

    private fun setupLetterList() {
        lettersListAdapter = LetterListAdapter(requireContext())

        lettersListAdapter.onItemClickListener = { letter ->
            val action = LetterListFragmentDirections.navigateToLetterGenerator(letter.id)
            findNavController().navigate(action)
        }

        binding.lettersRV.apply {
            adapter = lettersListAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

   /*     ItemTouchHelper(LetterListSwipeCallback(requireContext(), object : OnItemSwipe {
            override fun onSwiped(position: Int) {
                val item = lettersListAdapter.currentList[position]
                item.isDisabled = true
                loveItemsViewModel.updateLetter(item)
                val snackBar = Snackbar.make(binding.letterListCL, R.string.title_letter_deleted, Snackbar.LENGTH_LONG)
                snackBar.setAction(R.string.title_undo) {
                    item.isDisabled = false
                    loveItemsViewModel.updateLetter(item)
                }
                snackBar.show()
            }
        })).attachToRecyclerView(binding.lettersRV)*/
    }

}

